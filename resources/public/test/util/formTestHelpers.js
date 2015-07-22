
function enterValidValues(applicationPage) {
  applicationPage.setInputValue("organization", "Testi Organisaatio")()
  applicationPage.setInputValue("primary-email", "yhteyshenkilo@example.com")()
  applicationPage.waitAutoSave()
  applicationPage.setInputValue("signature", "Matti Allekirjoitusoikeudellinen")()
  applicationPage.setInputValue("signature-email", "matti.allekirjoitusoikeudellinen@example.com")()
  applicationPage.setInputValue("other-organizations.other-organizations-1.name", "Muu Testi Organisaatio")()
  applicationPage.setInputValue("other-organizations.other-organizations-1.email", "muutestiorganisaatio@example.com")()
  applicationPage.setInputValue("project-goals", "Hankkeen tavoitteet tulee tähän.")()
  applicationPage.setInputValue("project-description.project-description-1.goal", "Hankkeen ensimmäinen tavoite.")()
  applicationPage.setInputValue("project-description.project-description-1.activity", "Hankkeen ensimmäinen toiminta.")()
  applicationPage.setInputValue("project-description.project-description-1.result", "Hankkeen ensimmäinen tulos.")()
  applicationPage.setInputValue("project-target", "Kohderymämme on meidän kohderyhmä")()
  applicationPage.setInputValue("project-effectiveness", "Mittaamme vaikutusta.")()
  applicationPage.setInputValue("project-spreading-plan", "Jakelusuunnitelma.")()
  applicationPage.setInputValue("project-measure", "Mittaamme toteutumista ja vaikutusta.")()
  applicationPage.setInputValue("project-announce", "Tiedoitamme hankkeesta kivasti sitten.")()
  applicationPage.setInputValue("continuation-project", "no")()
  applicationPage.setInputValue("bank-iban", "FI 32 5000 4699350600")()
  applicationPage.setInputValue("bank-bic", "5000")()
  applicationPage.setInputValue("coordination-costs-row.amount", "0")()
  applicationPage.setInputValue("personnel-costs-row.amount", "0")()
  applicationPage.setInputValue("service-purchase-costs-row.amount", "0")()
  applicationPage.setInputValue("material-costs-row.amount", "0")()
  applicationPage.setInputValue("rent-costs-row.amount", "0")()
  applicationPage.setInputValue("equipment-costs-row.amount", "0")()
  applicationPage.setInputValue("steamship-costs-row.description", "Projektiorkesterin laivaus")()
  applicationPage.setInputValue("steamship-costs-row.amount", "10")()
  applicationPage.setInputValue("other-costs-row.amount", "0")()
  applicationPage.setInputValue("project-incomes-row.amount", "0")()
  applicationPage.setInputValue("eu-programs-income-row.amount", "0")()
  applicationPage.setInputValue("other-public-financing-income-row.amount", "0")()
  applicationPage.setInputValue("private-financing-income-row.amount", "0")()
}
