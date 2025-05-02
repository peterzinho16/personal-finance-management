SELECT COALESCE(ex.payee, ex.description)                  AS Descripcion,
       c.name                                              AS Tipo_Gasto,
       sc.name                                             AS Categoria_Gasto,
       TO_CHAR(ex.transaction_date, 'dd/MM/yyyy')          AS Fecha,
       NULL                                                AS Mes_Anio,
       ex.amount                                           AS Monto,
       case when ex.shared then 'SI' else 'NO' end         AS Gasto_compartido,
       null                                                AS Monto_shared,
       null                                                AS Monto_shared_dividido,
       NULL                                                AS Prestamo,
       NULL                                                AS Monto_Prestamo,
       ex.lent_to                                          AS Prestado_a,
       NULL                                                AS Estado_Deuda_Amore,
       case when ex.was_borrowed then ex.borrowed_from end AS Devolver_a,
       ex.borrowed_state                                   AS Estado_Devolucion,
       'Directo'                                           AS Tipo_Pago
FROM expenditures ex
         INNER JOIN public.sub_categories sc ON ex.sub_category_id = sc.id
         INNER JOIN categories c ON sc.category_id = c.id
WHERE transaction_date >= '05-03-2025'
ORDER BY ex.transaction_date ASC;


select round(sum(amount::numeric), 2)
from expenditures
where shared;

--Temp 9570.63 al dia 02/03/25 a las horas 14:21 PM

--Monthly report
     WITH subtot AS (SELECT to_char(transaction_date, 'YYYY-MM')             AS periodo,
                       round(
                               sum(CASE
                                       WHEN shared IS FALSE
                                           AND lent IS FALSE
                                           AND was_borrowed IS FALSE
                                           AND exp_imported IS FALSE
                                           THEN CASE
                                                    WHEN currency = 'PEN' THEN amount
                                                    ELSE conversion_to_pen
                                           END
                                       ELSE 0
                                   END)::NUMERIC, 2)                    AS Gastos_individuales,
                       round(sum(CASE
                                     WHEN shared IS TRUE
                                         THEN CASE
                                                  WHEN currency = 'PEN' THEN shared_amount
                                                  ELSE conversion_to_pen / 2
                                         END
                                     ELSE 0
                           END)::NUMERIC, 2)                            AS Gastos_Compartidos,
                       round(sum(CASE
                                     WHEN was_borrowed IS TRUE
                                         THEN CASE
                                                  WHEN currency = 'PEN' THEN amount
                                                  ELSE conversion_to_pen
                                         END
                                     ELSE 0
                           END)::NUMERIC, 2)                            AS Mis_Gastos_Pagados_Por_Tercero,
                       round(sum(CASE
                                     WHEN exp_imported IS TRUE
                                         THEN CASE
                                                  WHEN currency = 'PEN' THEN shared_amount
                                                  ELSE conversion_to_pen / 2
                                         END
                                     ELSE 0
                           END)::NUMERIC, 2)                            AS Mis_Gastos_Importados,
                       round(sum(CASE
                                     WHEN lent IS TRUE
                                         THEN CASE
                                                  WHEN currency = 'PEN' THEN loan_amount
                                                  ELSE conversion_to_pen
                                         END
                                     ELSE 0
                           END)::NUMERIC, 2)                            AS Total_Tus_Prestamos,
                       (SELECT sum(amount) FROM recurrent_expenditures) AS Gastos_Recurrentes_Total
                FROM expenditures
                GROUP BY to_char(transaction_date, 'YYYY-MM')
                order by 1 desc)
SELECT (SELECT round(SUM(CASE
                             WHEN currency = 'PEN' THEN amount
                             ELSE conversion_to_pen
    END)::NUMERIC, 2)
        FROM incomes
        WHERE was_received
          and to_char(received_date, 'YYYY-MM') = subtot.periodo)           AS otros_ingresos,
       subtot.Gastos_individuales + subtot.Gastos_Compartidos +
       subtot.Mis_Gastos_Pagados_Por_Tercero + subtot.Mis_Gastos_Importados AS Final_Total_Gastos,
       subtot.Gastos_Compartidos - subtot.Mis_Gastos_Importados AS GastosCompartidos_MenosImportados,
       subtot.Mis_Gastos_Importados
FROM subtot;

select *
from expenditures
where lent_to is not null
order by transaction_date;

--Total loans amount effectuated for me by person (Grouped)
select to_char(transaction_date, 'YYYY-MM') periodo,
       lent_to,
       round(sum(case when currency = 'PEN' then amount else amount * 3.75 end)::numeric, 2)
FROM expenditures
where lent = true
group by to_char(transaction_date, 'YYYY-MM'), lent_to
order by to_char(transaction_date, 'YYYY-MM') desc;

--Total loans amount effectuated from me by person (Detailed) WHERE loan is still not paid
select to_char(transaction_date, 'YYYY-MM') periodo,
       lent_to,
       description,
       round(case when currency = 'PEN' then amount else amount * 3.75 end::numeric, 2) monto,
       *
FROM expenditures
where lent = true
  AND loan_state != 'PAID'
order by to_char(transaction_date, 'YYYY-MM') desc, lent_to;


--Total expenses by month and with its total amount
select borrowed_from, round(sum(amount)::numeric, 2)
from expenditures
where transaction_date between '01-03-2025' and '01-04-2025'
  and borrowed_from is not null
group by borrowed_from;

--Update loan state after being paid
--1. List
select *
from public.expenditures
where transaction_date between '01-03-2025' and '01-04-2025'
  AND loan_state = 'PENDING'
order by transaction_date desc;
--2. Update
update expenditures
set loan_state='PAID'
where transaction_date between '01-03-2025' and '01-05-2025'
  AND loan_state = 'PENDING';

--Auditing shared expenditures
SELECT id,
       payee,
       description,
       transaction_date,
       CASE
           WHEN currency = 'USD' THEN expenditures.conversion_to_pen / 2
           ELSE shared_amount
           END AS shared_amount_in_soles,
       amount,
       currency,
       *
FROM public.expenditures
WHERE transaction_date BETWEEN '01-03-2025' and '01-04-2025'
  AND shared
ORDER BY transaction_date DESC;


-- Query to know the detail expenses of category Gastos_individuales and Gastos_Compartidos

SELECT transaction_date,
       sub_category_id,
       payee,
       description,
       amount,
       currency,
       CASE
           WHEN shared IS TRUE
               THEN CASE
                        WHEN currency = 'PEN' THEN shared_amount
                        ELSE conversion_to_pen / 2
               END
           WHEN shared IS FALSE
               AND lent IS FALSE
               AND was_borrowed IS FALSE
               THEN CASE
                        WHEN currency = 'PEN' THEN amount
                        ELSE conversion_to_pen
               END
           ELSE 0
           END amount_in_pen,
       installments
FROM expenditures
WHERE transaction_date between '01-03-2025' and '01-04-2025'
  AND ((shared = FALSE AND lent = FALSE AND was_borrowed = FALSE) -- Gastos_individuales
    OR (shared = TRUE)) -- Gastos_Compartidos
ORDER BY transaction_date DESC;
