const install = function(Vue, opts = {}) {
  Vue.prototype.$conjunction = {
    AND: '并且',
    OR: '或者'
  }
}

export default {
  install
}
