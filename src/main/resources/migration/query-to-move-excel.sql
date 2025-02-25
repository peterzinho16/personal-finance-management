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
WHERE transaction_date >= '2025-01-05'
ORDER BY ex.transaction_date ASC;


select round(sum(amount::numeric), 2)
from expenditures
where shared;

--Monthly report
select (select round(SUM(case when currency = 'PEN' then amount else amount * 3.75 end)::numeric, 2)
        from incomes
        where to_char(received_date, 'YYYY-MM') = periodo)                                            otros_ingresos,
       subtot.Gastos_individuales + subtot.Gastos_Compartidos +
       subtot.Mis_Gastos_Pagados_Por_Tercero                                                          Final_Total_Gastos,
       subtot.*
from (select to_char(transaction_date, 'YYYY-MM')             periodo,
             round(
                     sum(case
                             when
                                 shared is false and
                                 lent is false and
                                 was_borrowed is false
                                 then case when currency = 'PEN' then amount else amount * 3.75 end
                             else 0 end)::numeric,
                     2)                                       Gastos_individuales,
             round(sum(case
                           when shared is true
                               then case when currency = 'PEN' then shared_amount else shared_amount * 3.75 end
                           else 0 end)::numeric, 2)           Gastos_Compartidos,
             round(sum(case
                           when was_borrowed is true then case when currency = 'PEN' then amount else amount * 3.75 end
                           else 0 end)::numeric, 2)           Mis_Gastos_Pagados_Por_Tercero,
             round(sum(case
                           when lent is true
                               then case when currency = 'PEN' then loan_amount else loan_amount * 3.75 end
                           else 0 end)::numeric, 2)           Total_Tus_Prestamos,
             (select sum(amount) from recurrent_expenditures) Gastos_Recurrentes_Total
      from expenditures
      group by to_char(transaction_date, 'YYYY-MM')
      order by 1 desc) as subtot;

--Total loans amount effectuated for me by person (Grouped)
select to_char(transaction_date, 'YYYY-MM') periodo,
       lent_to,
       round(sum(case when currency = 'PEN' then amount else amount * 3.75 end)::numeric, 2)
FROM expenditures
where lent = true
group by to_char(transaction_date, 'YYYY-MM'), lent_to
order by to_char(transaction_date, 'YYYY-MM') desc;

--Total loans amount effectuated for me by person (Detailed)
select to_char(transaction_date, 'YYYY-MM') periodo,
       lent_to,
       description,
       round(case when currency = 'PEN' then amount else amount * 3.75 end::numeric, 2)
FROM expenditures
where lent = true
order by to_char(transaction_date, 'YYYY-MM') desc, lent_to;


--Total expenses by month and with its total amount
select borrowed_from, round(sum(amount)::numeric, 2)
from expenditures
where transaction_date between '2025-01-01' and '2025-01-31'
  and borrowed_from is not null
group by borrowed_from;

--Update loan state after being paid
--1. List
select *
from public.expenditures
where transaction_date between '01-01-2025' and '31-01-2025'
  AND loan_state = 'PENDING'
order by transaction_date desc;
--2. Update
update expenditures
set loan_state='PAID'
where transaction_date between '01-01-2025' and '31-01-2025'
  AND loan_state = 'PENDING';