const install = function(Vue, opts = {}) {
  Vue.prototype.$op = {
    items: [
      { value: 'EQ', text: '等于', enabled: ['Integer', 'Long', 'Double', 'BigDecimal', 'String', 'Boolean', 'Number', 'Date', 'Object', 'Derive'] },
      { value: 'NEQ', text: '不等于', enabled: ['Integer', 'Long', 'Double', 'BigDecimal', 'String', 'Boolean', 'Number', 'Date', 'Object', 'Derive'] },
      { value: 'GT', text: '大于', enabled: ['Integer', 'Long', 'Double', 'BigDecimal', 'Number', 'Date', 'String','Derive'] },
      { value: 'GTE', text: '大于或等于', enabled: ['Integer', 'Long', 'Double', 'BigDecimal', 'Number', 'Date', 'String','Derive'] },
      { value: 'LT', text: '小于', enabled: ['Integer', 'Long', 'Double', 'BigDecimal', 'Number', 'Date', 'String','Derive'] },
      { value: 'LTE', text: '小于或等于', enabled: ['Integer', 'Long', 'Double', 'BigDecimal', 'Number', 'Date', 'String','Derive'] },
      { value: 'MATCHES', text: '匹配正则表达式', enabled: ['String'] },
      { value: 'NOT_MATCHES', text: '不匹配正则表达式', enabled: ['String'] },
      { value: 'CONTAINS', text: '包含', enabled: ['String', 'List', 'Set'] },
      { value: 'NOT_CONTAINS', text: '不包含', enabled: ['String', 'List', 'Set'] },
      { value: 'STR_STARTSWITH', text: '开始于', enabled: ['String'] },
      { value: 'STR_NOT_STARTSWITH', text: '不开始于', enabled: ['String'] },
      { value: 'STR_ENDSWITH', text: '结束于', enabled: ['String'] },
      { value: 'STR_NOT_ENDSWITH', text: '不结束于', enabled: ['String'] },
      { value: 'STR_LENGTH', text: '长度为', enabled: ['String'] },
      { value: 'STR_NOT_LENGTH', text: '长度不为', enabled: ['String'] }
    ],
    getText(value) {
      let text
      this.items.some(e => {
        if (e.value == value) {
          text = e.text
          return true
        }
      })
      return text
    },
    filterItems(type) {
      if (!type) return this.items
      const dotIndex = type.indexOf('.')
      if (dotIndex != -1) type = type.substring(0, dotIndex)
      let items = []
      this.items.forEach(e => {
        if (e.enabled.indexOf(type) != -1) {
          items.push(e)
          return true
        }
      })
      return items
    }
  }
}

export default {
  install
}
