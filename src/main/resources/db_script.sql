CREATE TYPE client_type AS ENUM (
    'LEGAL',
    'INDIVIDUAL'
    );

CREATE TABLE public.client
(
    id          SERIAL PRIMARY KEY,
    name        VARCHAR            NOT NULL,
    surname     VARCHAR,
    login       VARCHAR,
    description VARCHAR,
    type        public.client_type NOT NULL,
    birth_date  timestamp          NOT NULL,
    updated     timestamp,
    created     timestamp          NOT NULL DEFAULT now()
);

CREATE TABLE public.attachment
(
    id        SERIAL PRIMARY KEY,
    client_id BIGINT REFERENCES public.client (id) ON DELETE CASCADE,
    content   BYTEA,
    type      VARCHAR,
    length    BIGINT
);

CREATE INDEX ON public.client (name, surname, login);
CREATE INDEX ON public.client (surname, login);
CREATE INDEX ON public.client (login);

/* For test population */
CREATE OR REPLACE FUNCTION populate_clients(n INTEGER) RETURNS VOID
    LANGUAGE plpgsql
AS
$$
DECLARE
    counter INTEGER := 0;
BEGIN
    LOOP
        EXIT WHEN counter = n;
        counter := counter + 1;
        INSERT INTO public.client (name, surname, login, description, type, birth_date)
        VALUES (CONCAT('Name ', counter),
                CONCAT('Surname ', counter),
                CONCAT('Login ', counter),
                CONCAT('Client Description ', counter),
                (SELECT * FROM unnest(enum_range(NULL::client_type)) ORDER BY random() LIMIT 1),
                (SELECT * FROM generate_series('1960-01-01'::timestamp, '2015-01-01'::timestamp, '1 day'::interval) ORDER BY random() LIMIT 1));
    END LOOP;
END;
$$;