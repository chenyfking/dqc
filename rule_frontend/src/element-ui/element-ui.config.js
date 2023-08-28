import Vue from 'vue';

import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import '../assets/theme/index.css'

Vue.use(ElementUI, {size: 'medium'});

['success', 'warning', 'info', 'error'].forEach(type => {
  Vue.prototype.$message[type] = options => {
    if (typeof options === 'string') {
      options = {
        message: options,
        showClose: true
      };
    }
    options.type = type;
    return Vue.prototype.$message(options);
  };
});

Vue.prototype.$dialogTop = '60px';
