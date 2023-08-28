import Vue from 'vue'
import Node from './node'
import ActionNodeVue from './action-node.vue'
import store from '../../../../vuex/index'

const VueConstructor = Vue.extend(ActionNodeVue);

export default class ActionNode extends Node {
  constructor(context, parentNode, data) {
    super(context, parentNode, data);
    this.init();
  }

  init() {
    const self = this;
    super.mount(new VueConstructor({
      store,
      propsData: {
        data: self.data.rhs
      },
      methods: {
        delete() {
          self.delete();
        },
        copyNode() {
          self.copyNode();
        },
        cutNode() {
          self.cutNode();
        },
        resize() {
          self.context.traverse();
        }
      }
    }));
  }
}
