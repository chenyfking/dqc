const install = function(Vue, opts = {}) {
  Vue.prototype.$attr = {
    items: [
      {name: 'salience', label: '优先级'},
      {name: 'date-effective', label: '生效日期'},
      {name: 'date-expires', label: '失效日期'},
      {name: 'activation-group', label: '互斥组'},
      {name: 'enabled', label: '是否可用'}
    ],
    getLabel(name) {
      let label
      this.items.some(e => {
        if (e.name == name) {
          label = e.label
          return true
        }
      })
      return label
    },
    getName(label) {
      let name
      this.items.some(e => {
        if (e.label == label) {
          name = e.name
          return true
        }
      })
      return name
    }
  }
}

export default {
  install
}
