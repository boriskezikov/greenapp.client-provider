package com.client.provider.controller;

import com.client.provider.logic.CreateClientOperation;
import com.client.provider.logic.CreateClientOperation.AttachPhotoRequest;
import com.client.provider.logic.CreateClientOperation.CreateClientRequest;
import com.client.provider.logic.EditClientOperation;
import com.client.provider.logic.EditClientOperation.EditClientRequest;
import com.client.provider.logic.FindAttachmentsByClientIdOperation;
import com.client.provider.logic.FindAttachmentsByClientIdOperation.FindAttachmentsByClientIdRequest;
import com.client.provider.logic.FindClientByIdOperation;
import com.client.provider.logic.FindClientByIdOperation.FindClientByIdRequest;
import com.client.provider.logic.FindClientsOperation;
import com.client.provider.logic.FindClientsOperation.FindClientsRequest;
import com.client.provider.model.Client;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import javax.validation.Valid;

@org.springframework.web.bind.annotation.RestController
@RequestMapping("/client-provider")
@RequiredArgsConstructor
public class RestController {

    private final static Logger log = LoggerFactory.getLogger(RestController.class);

    private final CreateClientOperation createClientOperation;
    private final FindClientsOperation findClientsOperation;
    private final FindClientByIdOperation findClientByIdOperation;
    private final FindAttachmentsByClientIdOperation findAttachmentsByClientIdOperation;
    private final EditClientOperation editClientOperation;

    @PostMapping("/clients")
    public Flux<Client> findClients(@Valid @RequestBody(required = false) FindClientsRequest request) {
        return Mono.just(request)
            .flatMapMany(findClientsOperation::process)
            .doOnSubscribe(s -> log.info("RestController.findClients.in request = {}", request))
            .doOnComplete(() -> log.info("RestController.findClients.out"));
    }

    @GetMapping("/client/{id}")
    public Mono<Client> getClientById(@PathVariable(value = "id") Long taskId) {
        return Mono.just(new FindClientByIdRequest(taskId))
            .flatMap(findClientByIdOperation::process)
            .doOnSubscribe(s -> log.info("RestController.getClientById.in id = {}", taskId))
            .doOnSuccess(s -> log.info("RestController.getClientById.out"));
    }

    @PostMapping(value = "/client",
        consumes = {
            MediaType.MULTIPART_FORM_DATA_VALUE
        })
    public Mono<Long> createClient(@RequestPart("client") Client client,
                                   @RequestPart(value = "attachment", required = false) MultipartFile attachment) {
        var request = Mono.just(attachment)
            .flatMap(a -> {
                try {
                    return Mono.just(new AttachPhotoRequest(a.getContentType(), a.getSize(), a.getBytes()));
                } catch (IOException e) {
                    return Mono.error(e);
                }
            });
        return request.map(a -> new CreateClientRequest(client, a))
            .flatMap(createClientOperation::process)
            .doOnSubscribe(s -> log.info("RestController.createClient.in client = {}", client))
            .doOnSuccess(s -> log.info("RestController.createClient.out"));
    }

    @PutMapping(value = "/client", params = "detach")
    public Mono<Void> editClient(@RequestPart("client") Client client,
                                 @RequestPart(value = "attachment", required = false) MultipartFile attachment,
                                 @RequestParam(value = "detach", required = false, defaultValue = "false") boolean detach) {
        var request = Mono.just(attachment)
            .flatMap(a -> {
                try {
                    return Mono.just(new AttachPhotoRequest(a.getContentType(), a.getSize(), a.getBytes()))
                        .map(r -> {
                            r.setClientId(client.id);
                            return r;
                        });
                } catch (IOException e) {
                    return Mono.error(e);
                }
            });
        return request.map(a -> new EditClientRequest(client, a, detach))
            .flatMap(editClientOperation::process)
            .doOnSubscribe(s -> log.info("RestController.editClient.in client = {}", client))
            .doOnSuccess(s -> log.info("RestController.editClient.out"));
    }

    @GetMapping(value = "/client/{id}/attachment", produces = {
        MediaType.MULTIPART_FORM_DATA_VALUE
    })
    @ResponseBody
    public Mono<MultiValueMap<String, HttpEntity<?>>> findAttachments(@PathVariable(value = "id") Long id) {
        return Mono.just(new FindAttachmentsByClientIdRequest(id))
            .flatMapMany(findAttachmentsByClientIdOperation::process)
            .collectList()
            .map(l -> {
                var builder = new MultipartBodyBuilder();
                l.forEach(a -> builder.part(
                    "attachment",
                    a.content,
                    MediaType.valueOf(a.contentType)
                ));
                return builder.build();
            })
            .doOnSubscribe(s -> log.info("RestController.findAttachments.in id = {}", id))
            .doOnSuccess(s -> log.info("RestController.findAttachments.out"));
    }
}
