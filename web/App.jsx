import PolyfillBind from './polyfill-bind'
import React from 'react'

import FormContainer from './FormContainer.jsx'
import FormModel from './FormModel'

const model = new FormModel()
const formModelP = model.init()

formModelP.onValue((state) => {
  console.log("Updating UI with state:", state)
  React.render(
    <FormContainer model={model} form={state.form} infoElementValues={state.avustushaku} validationErrors={state.validationErrors} translations={state.translations} values={state.values} valuesId={state.valuesId} lang={state.lang} preview={state.preview} />,
    document.getElementById('app')
  )
})
