import React from 'react'
import _ from 'lodash'

import FormUtil from './../FormUtil.js'
import LocalizedString from './LocalizedString.jsx'
import Translator from './../Translator.js'

export default class FormErrorSummary extends React.Component {

  constructor(props) {
    super(props)
    this.translations = this.props.translations
    this.handleClick = this.handleClick.bind(this)
    this.state = { open: false }
  }

  handleClick() {
    this.setState({
      open: !this.state.open
    })
  }

  render() {
    const lang = this.props.lang
    const formContent = this.props.formContent
    const validationErrors = this.props.validationErrors
    const translator = new Translator(this.translations)
    const saveError = this.props.saveError.length > 0 ? translator.translate(this.props.saveError, lang) : ""
    const idsOfInvalidFields = _.keys(validationErrors);
    const idsWithErrors = _(idsOfInvalidFields).
                            map(id => { return { fieldId: id, errors: validationErrors[id] } }).
                            filter(idWithErrors => { return idWithErrors.errors.length !== 0} ).
                            value()
    const invalidFieldsCount = idsWithErrors.length
    const fieldErrorMessageElements = _.map(idsWithErrors, idWithErrors => {
      const closestParent = FormUtil.findFieldWithDirectChild(formContent, idWithErrors.fieldId)
      return this.renderFieldErrors(FormUtil.findField(formContent, idWithErrors.fieldId), closestParent, idWithErrors.errors, lang)
    })
    return (
      <div id="form-error-summary" hidden={invalidFieldsCount === 0 && saveError.length === 0}>
        <div hidden={saveError.length === 0} className="error">{saveError}</div>
        <a onClick={this.handleClick} role="button" className="error" id="validation-errors-summary" hidden={invalidFieldsCount === 0}>
          {translator.translate("validation-errors", lang, null, {kpl: invalidFieldsCount})}
        </a>
        <div className="popup" hidden={!this.state.open || invalidFieldsCount === 0} id="validation-errors">
          <a role="button" className="popup-close" onClick={this.handleClick}>&times;</a>
          {fieldErrorMessageElements}
        </div>
      </div>
    )
  }

  renderFieldErrors(field, closestParent, errors, lang) {
    const fieldErrors = []
    const labelHolder = field.label ? field : closestParent
    for (var i = 0; i < errors.length; i++) {
      const error = errors[i]
      const key = field.id + "-validation-error-" + error.error
      if (fieldErrors.length > 0) {
        fieldErrors.push(<span key={key + "-separator"}>, </span>)
      }
      fieldErrors.push(<LocalizedString key={key} translations={this.translations} translationKey={error.error}
                                        lang={lang}/>)
    }
    return <div className="error" key={field.id + "-validation-error"}>
      <LocalizedString translations={labelHolder} translationKey="label" defaultValue={field.id} lang={lang}/><span>: </span>
      {fieldErrors}
    </div>
  }
}