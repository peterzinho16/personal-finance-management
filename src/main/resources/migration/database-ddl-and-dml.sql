-- Data Definition Language
create table public.categories
(
    id   integer generated by default as identity
        primary key,
    name varchar(255) not null
);

alter table public.categories
    owner to postgres;

create unique index unique_name_ci
    on public.categories (lower(name::text));

create table public.sub_categories
(
    id          integer generated by default as identity
        primary key,
    name        varchar(255) not null,
    category_id integer      not null
        references public.categories
);

alter table public.sub_categories
    owner to postgres;

create unique index unique_name_n_category_id
    on public.sub_categories (lower(name::text), category_id);



create table public.expenditures
(
    id               serial
        primary key,
    description      text             not null,
    sub_category_id  integer          not null
        references public.sub_categories,
    recurrent        boolean  default false,
    transaction_date timestamp        not null,
    amount           double precision not null,
    currency         text
        constraint expenditures_currency_check
            check (currency = ANY (ARRAY['PEN'::text, 'USD'::text])),
    shared           boolean  default false,
    shared_amount    double precision,
    single_payment   boolean  default true,
    installments     smallint default 1,
    lent             boolean  default false,
    lent_to          text,
    loan_state       text
        constraint expenditures_loan_state_check
            check (loan_state = ANY (ARRAY['pending'::text, 'paid'::text])),
    loan_amount      double precision
);

alter table public.expenditures
    owner to postgres;

create index idx_expenditures_sub_category_id
    on public.expenditures (sub_category_id);

create index idx_expenditures_transaction_date
    on public.expenditures (transaction_date);

-- Master tables
-- First, insert all categories
INSERT INTO categories (name)
VALUES ('Movilidad'),
       ('Vestimenta'),
       ('Alimentacion'),
       ('Salidas'),
       ('Obsequios'),
       ('Servicio'),
       ('Educacion'),
       ('Viaje'),
       ('Salud'),
       ('Hogar'),
       ('Por definir');

-- Then, insert subcategories with references to their parent categories
INSERT INTO sub_categories (name, category_id)
SELECT 'Combustible', id
FROM categories
WHERE name = 'Movilidad'
UNION ALL
SELECT 'Estacionamiento', id
FROM categories
WHERE name = 'Movilidad'
UNION ALL
SELECT 'Reparaciones', id
FROM categories
WHERE name = 'Movilidad'
UNION ALL
SELECT 'Peajes', id
FROM categories
WHERE name = 'Movilidad'
UNION ALL
SELECT 'Articulos Auto', id
FROM categories
WHERE name = 'Movilidad'
UNION ALL
SELECT 'Seguro auto', id
FROM categories
WHERE name = 'Movilidad'
UNION ALL
SELECT 'Calzado', id
FROM categories
WHERE name = 'Vestimenta'
UNION ALL
SELECT 'Prendas', id
FROM categories
WHERE name = 'Vestimenta'
UNION ALL
SELECT 'Accesorios', id
FROM categories
WHERE name = 'Vestimenta'
UNION ALL
SELECT 'Comida saludable', id
FROM categories
WHERE name = 'Alimentacion'
UNION ALL
SELECT 'Comida rapida', id
FROM categories
WHERE name = 'Alimentacion'
UNION ALL
SELECT 'Desayuno', id
FROM categories
WHERE name = 'Alimentacion'
UNION ALL
SELECT 'Paseo', id
FROM categories
WHERE name = 'Salidas'
UNION ALL
SELECT 'Restaurantes', id
FROM categories
WHERE name = 'Salidas'
UNION ALL
SELECT 'Cine', id
FROM categories
WHERE name = 'Salidas'
UNION ALL
SELECT 'Teatro', id
FROM categories
WHERE name = 'Salidas'
UNION ALL
SELECT 'Cumpleaños', id
FROM categories
WHERE name = 'Obsequios'
UNION ALL
SELECT 'Baby Shower', id
FROM categories
WHERE name = 'Obsequios'
UNION ALL
SELECT 'Navidad', id
FROM categories
WHERE name = 'Obsequios'
UNION ALL
SELECT 'Detalle', id
FROM categories
WHERE name = 'Obsequios'
UNION ALL
SELECT 'Aniversarios', id
FROM categories
WHERE name = 'Obsequios'
UNION ALL
SELECT 'Esteticos', id
FROM categories
WHERE name = 'Servicio'
UNION ALL
SELECT 'Limpieza hogar', id
FROM categories
WHERE name = 'Servicio'
UNION ALL
SELECT 'Plataformas digitales', id
FROM categories
WHERE name = 'Servicio'
UNION ALL
SELECT 'Hogar (Luz, Agua, Mantenimiento, Internet)', id
FROM categories
WHERE name = 'Servicio'
UNION ALL
SELECT 'Cursos', id
FROM categories
WHERE name = 'Educacion'
UNION ALL
SELECT 'Certificados', id
FROM categories
WHERE name = 'Educacion'
UNION ALL
SELECT 'Idiomas', id
FROM categories
WHERE name = 'Educacion'
UNION ALL
SELECT 'Plataformas digitales (Blinkist, Medium)', id
FROM categories
WHERE name = 'Educacion'
UNION ALL
SELECT 'Hospedaje', id
FROM categories
WHERE name = 'Viaje'
UNION ALL
SELECT 'Tickets Avion', id
FROM categories
WHERE name = 'Viaje'
UNION ALL
SELECT 'Tickets Bus', id
FROM categories
WHERE name = 'Viaje'
UNION ALL
SELECT 'Medicacion', id
FROM categories
WHERE name = 'Salud'
UNION ALL
SELECT 'Consultas', id
FROM categories
WHERE name = 'Salud'
UNION ALL
SELECT 'Decoracion', id
FROM categories
WHERE name = 'Hogar'
UNION ALL
SELECT 'Remodelacion', id
FROM categories
WHERE name = 'Hogar'
UNION ALL
SELECT 'Muebles', id
FROM categories
WHERE name = 'Hogar'
UNION ALL
SELECT 'Vajilla', id
FROM categories
WHERE name = 'Hogar'
UNION ALL
SELECT 'Electrodomesticos', id
FROM categories
WHERE name = 'Hogar'
UNION ALL
SELECT 'Por definir', id
FROM categories
WHERE name = 'Por definir';

-- Validation
select c.name, sc.name
from sub_categories sc
         inner join public.categories c on sc.category_id = c.id;