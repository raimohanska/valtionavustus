import React from 'react'
import _ from 'lodash'

import FormSaveStatus from '../form/component/FormSaveStatus.jsx'
import ToggleLanguageButton from '../form/component/ToggleLanguageButton.jsx'
import LocalizedString from '../form/component/LocalizedString.jsx'
import FormErrorSummary from '../form/component/FormErrorSummary.jsx'

export default class VaFormTopbar extends React.Component {
  render() {
    const controller = this.props.controller
    const state = this.props.state
    const saveStatus = state.saveStatus
    const configuration = state.configuration
    const form = state.form
    const validationErrors = state.validationErrors
    const lang = configuration.lang
    const translations = configuration.translations
    const preview = configuration.preview
    const formIsValid = _.reduce(state.clientSideValidation, function (allValid, valid) {
      return allValid === true && valid === true
    }, true)
    const formOperations = state.extensionApi.formOperations
    const openPreview = function() {
      window.open(formOperations.urlCreator.existingSubmissionPreviewUrl(state), "preview")
    }

    return(
      <section id="topbar">
        <div id="top-container">
          <img id="logo" src="img/logo.png"/>
          <h1 id="topic"><LocalizedString translations={translations.form} translationKey="heading" lang={lang}/></h1>
          <div id="form-controls" hidden={preview}>
            <FormSaveStatus saveStatus={saveStatus} translations={translations} lang={lang}/>
            <button id="submit" type="submit" className="soresu-text-button" onClick={controller.submit} disabled={!(formIsValid && controller.isSaveDraftAllowed(state)) || controller.hasPendingChanges(state)}>
              <LocalizedString translations={translations.form} translationKey="submit" lang={lang}/>
            </button>
                <span id="form-controls-devel" hidden={!configuration.develMode}>
                  <ToggleLanguageButton id="toggle-language" controller={controller} languages={translations.languages} lang={lang}/>
                  <button type="button" className="soresu-text-button" onClick={openPreview} disabled={!controller.isSaveDraftAllowed(state)}>
                    <LocalizedString translations={translations.form} translationKey="preview" lang={lang} />
                  </button>
                </span>
            <FormErrorSummary formContent={form.content} controller={controller} saveError={saveStatus.saveError} validationErrors={validationErrors} translations={translations.errors} lang={lang} />
          </div>
        </div>
        <span hidden={true} id="entity-id">{formOperations.printEntityId(state)}</span>
        <span hidden={true} id="pending-changes">{ controller.hasPendingChanges(state) ? "true" : "false"}</span>
      </section>
    )
  }
}