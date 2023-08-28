import store from '@/vuex/index.js'

const lhs = {
  getText(lhs, needLeft = false, html = true, loopTarget = null) {
    if (!lhs || !lhs.constraint) return '无'
    let text = this.getJoinConstraintText(lhs.constraint, needLeft, html, loopTarget)
    if (!text) return '无'
    while (text.startsWith('(') && text.endsWith(')')) {
      text = text.substring(1, text.length - 1)
    }
    return text
  },
  getJoinConstraintText(constraint, needLeft, html, loopTarget = null) {
    if (constraint.constraints.length == 0) return null
    const constraintsText = []
    constraint.constraints.forEach(e => {
      let text = this.getConstraintText(e, needLeft, html, loopTarget)
      if (text) constraintsText.push(text)
    })
    if (constraintsText.length == 0) return null;

    if (html) {
      if (constraintsText.length > 1) {
        return '(' + constraintsText.join('<span style="color: #303133;"> ' + this.$conjunction[constraint.conjunction] + ' </span>') + ')'
      }
      return constraintsText.join('<span style="color: #303133;"> ' + this.$conjunction[constraint.conjunction] + ' </span>')
    }

    if (constraint.conjunction == 'NOT') {
      if (constraintsText.length > 1) {
        return '!(' + constraintsText.join(' 或者 ') + ')'
      }
      return '!' + constraintsText.join(' 或者 ')
    }
    if (constraintsText.length > 1) {
      return '(' + constraintsText.join(' ' + this.$conjunction[constraint.conjunction] + ' ') + ')'
    }
    return constraintsText.join(' ' + this.$conjunction[constraint.conjunction] + ' ')
  },
  getConstraintText(constraint, needLeft, html, loopTarget = null) {
    if (constraint.conjunction) {
      return this.getJoinConstraintText(constraint, needLeft, html, loopTarget)
    } else {
      return this.getAConstraintText(constraint, needLeft, html, loopTarget)
    }
  },
  getAConstraintText(constraint, needLeft, html, loopTarget = null) {
    const leftText = this.getExpressionText(constraint.left, loopTarget)
    if (needLeft && this.$utils.isBlank(leftText)) return null
    const opText = this.$op.getText(constraint.op)
    if (this.$utils.isBlank(opText)) return null
    const rightText = this.getExpressionText(constraint.right, loopTarget)
    if (this.$utils.isBlank(rightText)) return null
    let text = ''
    if (html) {
      if (leftText) text += '<span style="color: rgb(180, 95, 4);">' + leftText + '</span> '
      text += '<span style="color: red;">' + opText + '</span>'
      text += ' <span style="color: rgb(180, 95, 4);">' + rightText + '</span>'
    } else {
      if (leftText) text += leftText + ' '
      text += opText
      text += ' ' + rightText
    }
    return text
  },
  getExpressionText(expr, loopTarget = null) {
    if (!expr) return null
    let text
    if (expr.type == 'INPUT') {
      text = this.getInputExpressionText(expr.input)
    } else if (expr.type == 'FACT') {
      text = this.getFactExpressionText(expr.fact)
    } else if (expr.type == 'CONSTANT') {
      text = this.getConstantExpressionText(expr.constant)
    } else if (expr.type == 'FUNC') {
      text = this.getFuncExpressionText(expr.func, loopTarget)
    } else if (expr.type == 'MODEL') {
      text = this.getModelExpressionText(expr.model)
    } else if (expr.type == 'COLLECTION_CHILD') {
      text = this.getCollectionChildExpressionText(expr.collectionChild, loopTarget)
    }
    if (this.$utils.isBlank(text)) return null
    if (expr.arith && Object.keys(expr.arith).length > 0) {
      const arithText = this.getArithText(expr.arith, loopTarget)
      if (!arithText) return null
      return text + ' ' + arithText
    }
    return text
  },
  getInputExpressionText(input) {
    if (!input || this.$utils.isBlank(input.value)) return null
    if (this.$utils.isBoolean(input.type)) {
      return input.value === 'true' ? '是' : '否'
    }
    return input.value
  },
  getFactExpressionText(fact) {
    if (!fact) return null
    return this.$store.getters.getFactText(fact.id, fact.fieldId)
  },
  getConstantExpressionText(constant) {
    if (!constant) return null
    return this.$store.getters.getConstantText(constant.id, constant.fieldId)
  },
  getFuncExpressionText(func, loopTarget = null) {
    if (!func) return null
    const paramsText = []
    func.params.forEach(param => {
      const paramText = this.getParamExpressionText(param, loopTarget)
      if (paramText) paramsText.push(paramText)
    })
    if (func.params.length > 0 && paramsText.length == 0) return null
    return func.method + '(' + paramsText.join(', ') + ')'
  },
  getParamExpressionText(param, loopTarget = null) {
    const value = param.value
    if (!value) return null
    return this.getExpressionText(value, loopTarget)
  },
  getModelExpressionText(model) {
    if (!model) return null
    const modelName = this.$store.getters.getModelName(model.id)
    if (!modelName) return null
    const input = model.input
    if (!input) return null
    const output = model.output
    if (!output) return null
    const inputText = this.getFactExpressionText(input)
    if (!inputText) return null
    const outputText = this.getFactExpressionText(output)
    if (!outputText) return null
    return modelName + '(' + inputText + ', ' + outputText + ')'
  },
  getCollectionChildExpressionText(child, loopTarget) {
    if (!loopTarget) return null
    const loopField = this.$store.getters.getFactField(loopTarget.id, loopTarget.fieldId)
    if (!loopField) return null
    if (this.$utils.isBaseType(loopField.subType)) return loopField.label
    return this.$store.getters.getFactText(child.id, child.fieldId)
  },
  getArithText(arith, loopTarget = null) {
    if (!arith.type) return null
    const exprText = this.getExpressionText(arith.right, loopTarget)
    if (!exprText) return null
    let typeText
    if (arith.type == 'ADD') {
      typeText = '+'
    } else if (arith.type == 'SUB') {
      typeText = '-'
    } else if (arith.type == 'MUL') {
      typeText = 'x'
    } else if (arith.type == 'DIV') {
      typeText = '÷'
    }
    if (arith.parentheses) return typeText + ' (' + exprText + ')'
    return typeText + ' ' + exprText
  }
}

const install = function(Vue, opts = {}) {
  lhs.$op = Vue.prototype.$op
  lhs.$conjunction = Vue.prototype.$conjunction
  lhs.$utils = Vue.prototype.$utils
  lhs.$store = store
  Vue.prototype.$lhs = lhs
}

export default {
  install
}
