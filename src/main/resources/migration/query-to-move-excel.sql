SELECT
    COALESCE(ex.payee, ex.description) AS  Descripcion ,
    c.name AS  Tipo_Gasto ,
    sc.name AS  Categoria_Gasto ,
    TO_CHAR(ex.transaction_date, 'dd/MM/yyyy') AS  Fecha ,
    NULL AS  Mes_Anio ,
    ex.amount AS  Monto ,
    case when ex.shared then 'SI' else 'NO' end AS  Gasto_compartido ,
    null AS  Monto_shared ,
    null AS  Monto_shared_dividido ,
    NULL AS  Prestamo ,
    NULL AS  Monto_Prestamo ,
    NULL AS  Prestado_a ,
    NULL AS  Estado_Deuda_Amore ,
    NULL AS  Devolver_a ,
    NULL AS  Estado_Devolucion ,
    'Directo' AS  Tipo_Pago
FROM expenditures ex
         INNER JOIN public.sub_categories sc ON ex.sub_category_id = sc.id
         INNER JOIN categories c ON sc.category_id = c.id
WHERE transaction_date >= '2024-12-31'
ORDER BY ex.transaction_date ASC;


select distinct name
from sub_categories order by 1 asc;
