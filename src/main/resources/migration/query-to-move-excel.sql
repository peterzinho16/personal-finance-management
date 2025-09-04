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
                                     WHEN shared IS TRUE and (exp_imported is false or expenditures.exp_imported is null)
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
SELECT periodo,(SELECT round(SUM(CASE
                             WHEN currency = 'PEN' THEN amount
                             ELSE conversion_to_pen
    END)::NUMERIC, 2)
        FROM incomes
        WHERE was_received
          and to_char(received_date, 'YYYY-MM') = subtot.periodo)           AS otros_ingresos,
       subtot.Gastos_individuales + subtot.Gastos_Compartidos +
       subtot.Mis_Gastos_Pagados_Por_Tercero AS Final_Total_Gastos,
       subtot.Gastos_Compartidos,
       subtot.Mis_Gastos_Importados Mis_Gastos_Compartidos_Importados,
       subtot.Gastos_Compartidos - subtot.Mis_Gastos_Importados Gastos_A_Devolver
FROM subtot;


--Total loans amount effectuated for me by person (Grouped)
select to_char(transaction_date, 'YYYY-MM') periodo,
       lent_to,
       round(sum(case when currency = 'PEN' then amount else conversion_to_pen end)::numeric, 2)
FROM expenditures
where lent = true
group by to_char(transaction_date, 'YYYY-MM'), lent_to
order by to_char(transaction_date, 'YYYY-MM') desc;

--Total loans amount effectuated from me by person (Detailed) WHERE loan is still not paid
select to_char(transaction_date, 'YYYY-MM') periodo,
       lent_to,
       description,
       round(case when currency = 'PEN' then amount else conversion_to_pen end::numeric, 2) monto,
       *
FROM expenditures
where lent = true
  AND loan_state != 'PAID'
order by to_char(transaction_date, 'YYYY-MM') desc, lent_to;


--Recover the total borrowed amount, grouped by individual. (Pending to be paid from me)
--1. List
select to_char(transaction_date, 'YYYY-MM') periodo, borrowed_from, round(sum(amount)::numeric, 2)
from expenditures
where transaction_date between '01-07-2025' and '01-09-2025'
  and borrowed_from is not null
    and borrowed_state != 'PAID'
group by to_char(transaction_date, 'YYYY-MM'), borrowed_from;
--2. Update (after payment made)
update expenditures set borrowed_state = 'PAID'
where transaction_date between '01-07-2025' and '01-09-2025'
  and borrowed_from is not null
  and borrowed_state = 'PENDING';

--Update loan state after being paid
--1. List
select id, lent_to, round(case when currency = 'PEN' then amount else conversion_to_pen end::numeric, 2) monto, payee, description
from public.expenditures
where transaction_date between '01-07-2025' and '01-09-2025'
  AND loan_state = 'PENDING'
order by transaction_date desc;
--2. Update
update expenditures
set loan_state='PAID'
where transaction_date between '01-07-2025' and '01-09-2025'
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
WHERE transaction_date BETWEEN '01-07-2025' and '01-09-2025'
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

select *
from expenditure_others order by transaction_date desc;

--incomes to get paid (check in)
select * from incomes where was_received is false;