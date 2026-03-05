# Backlog project

### Expenditure Menu

- Refactor: For CRUD operations consider made a pre-validation of ownership by session userId - 04/03/26 ✅
- Refactor: Parameters table to make it individual instead of general as is now - 04/03/26
- Refactor: Payee categorization logic

### Authentication module features

- Implement: **Temporary account lock** after 5 failed login attempts
- Implement: CSRF token ✅ - 08/02/26

### Refactor resume > installments

Divide the installments into two different views:
- **Installments pending** - List of installments that are pending to be full paid ✅20/11/25
- **Installments full-paid** - List of installments that are full paid ✅ - 20/11/25

### Styles and script importation

Download CSS/JS files to be consumed as local assets from resources ✅ - 17/10/25

### Chart view
- Update the chart queries considering the new column conversionToPen ✅ - 11/05/25

### Manual registration
- Check the manual registration if is updated with the new column conversionToPen ✅ - 12/05/25

### Bugs
- When doing sync, there is a scenario where isn't possible to extract payee, currency and amount because the emails isn't an expenditure email. We need to control the scenario to filter out those records ✅ 08/06/25

### News
- Implement a **consolidation page** to have a statement view-like
- Implement a feature in expenditure form to capture voice and complete all the details of the expenditure
- Implement a feature in expenditure form to OCR the receipt and pre-load the details of the expenditure
- Implement a list to show those incomes that are pending to be received

### Reference

[Admin Web Page: https://keenthemes.com/metronic/concepts/vite/store-inventory/category-details]