import React from 'react'
import LocalizedString from './LocalizedString.jsx'
import Translator from './Translator.js'
import FormElementError from './FormElementError.jsx'

class BasicFieldComponent extends React.Component {

  validate(value) {
    var validationErrors = []
    if(this.props.field.required && !value) {
      validationErrors = [{error: "required"}]
    }
    return validationErrors
  }

  createChangeListener() {
    const component = this
    return function(event) {
      const value = event.target.value
      component.props.model.setFieldValue(component.props.field.id, value, component.validate(value))
    }
  }

  componentDidMount() {
    this.props.model.setFieldValid(this.props.field.id, this.validate(this.props.value))
  }

  param(param, defaultValue) {
    if(!this.props.field.params) return defaultValue
    if(this.props.field.params[param] !== undefined) return this.props.field.params[param]
    return defaultValue
  }
}

class BasicTextField extends BasicFieldComponent {
  render() {
    const field = this.props.field
    return (<input
              type="text"
              id={field.id}
              name={field.id}
              required={field.required}
              size={this.param("size", this.param("maxlength",80))}
              maxLength={this.param("maxlength")}
              model={this.props.model}
              value={this.props.value}
              onChange={this.createChangeListener()}
            />)
  }
}

class EmailTextField extends BasicFieldComponent {
  constructor(props) {
    super(props)
    // Pretty basic regexp, allows anything@anything.anything
    this.validEmailRegexp = /\S+@\S+\.\S+/
  }

  render() {
    const field = this.props.field
    return (<input
      type="email"
      id={field.id}
      name={field.id}
      required={field.required}
      size={this.param("size", this.param("maxlength",80))}
      maxLength={this.param("maxlength")}
      model={this.props.model}
      value={this.props.value}
      onChange={this.createChangeListener()}
      />)
  }

  validate(value) {
    var validationErrors = super.validate(value)
    if (value) {
      const emailError = this.validateEmail(value)
      if (emailError) {
        validationErrors.push(emailError)
      }
    }
    return validationErrors
  }

  validateEmail(input) {
    return this.validEmailRegexp.test(input) ? undefined : {error: "email"};
  }
}

class BasicTextArea extends BasicFieldComponent {
  render() {
    const field = this.props.field
    return (<textarea
              id={field.id}
              name={field.id}
              required={field.required}
              rows={this.param("rows", 10)}
              cols={this.param("cols", 70)}
              maxLength={this.param("maxlength")}
              model={this.props.model}
              value={this.props.value}
              onChange={this.createChangeListener()}
            />)
  }
}

class Dropdown extends BasicFieldComponent {
  render() {
    const field = this.props.field
    const options = [];
    if(field.options) {
      for (var i=0; i < field.options.length; i++) {
        const label = new Translator(field.options[i]).translate("label", this.props.lang, field.options[i].value)
        options.push(<option key={field.id + "." + field.options[i].value} value={field.options[i].value}>
                      {label}
                     </option>)
      }
    }
    return (<select id={field.id} name={field.id} required={field.required} model={this.props.model} onChange={this.createChangeListener()} value={this.props.value}>
              {options}
            </select>)
  }
}

class RadioButton extends BasicFieldComponent {
  render() {
    const field = this.props.field
    const radiobuttons = [];

    if(field.options) {
      for (var i=0; i < field.options.length; i++) {
        const label = new Translator(field.options[i]).translate("label", this.props.lang, field.options[i].value)
        radiobuttons.push(<input {...this.props} type="radio" id={field.id + ".radio." + i} key={field.id + "." + field.options[i].value} name={field.id} required={field.required} value={field.options[i].value} onChange={this.createChangeListener()} checked={field.options[i].value === this.props.value ? true: null}/>)
        radiobuttons.push(<label key={field.id + "." + field.options[i].value + ".label"} htmlFor={field.id + ".radio." + i}>{label}</label>)
      }
    }
    return (<div>{radiobuttons}</div>)
  }
}

export default class FormElement extends React.Component {

  constructor(props) {
    super(props)
    this.fieldTypeMapping = {
      "textField": BasicTextField,
      "textArea": BasicTextArea,
      "emailField": EmailTextField,
      "dropdown": Dropdown,
      "radioButton": RadioButton
    }
  }

  render() {
    const field = this.props.field;
    const displayAs = field.displayAs
    var input = <span>Unsupported field type {displayAs}</span>

    if (displayAs in this.fieldTypeMapping) {
      input = React.createElement(this.fieldTypeMapping[displayAs], this.props)
    }
    return (
      <div>
        <label htmlFor={field.id} className={field.required ? "required" : ""}><LocalizedString  translations={field} translationKey="label" lang={this.props.lang} /></label>
        {input}
        <FormElementError fieldId={field.id} validationErrors={this.props.validationErrors} translations={this.props.translations} lang={this.props.lang}/>
      </div>
    )
  }
}
