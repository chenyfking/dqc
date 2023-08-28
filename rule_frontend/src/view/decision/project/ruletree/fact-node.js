import Vue from 'vue'
import Node from './node'
import FactNodeVue from './fact-node.vue'
import store from '../../../../vuex/index'

const VueConstructor = Vue.extend(FactNodeVue);

export default class FactNode extends Node {
  constructor(context, parentNode, data) {
    super(context, parentNode, data);
    this.init();
  }

  init() {
    const self = this
    super.mount(new VueConstructor({
      store,
      propsData: {
        data: self.data.fact
      },
      methods: {
        addChild(condition = {}) {
          const childData = {
            nodeType: 'CONDITION',
            condition: condition
          }
          if (!self.data.children) {
            self.data.children = [];
          }
          self.data.children.push(childData);
          return self.addChild(childData);
        },
        delete() {
          self.delete();
        },
        resetInput() {
          self.children.forEach(child => child.initInputType());
        },
        isRootNode() {
          return self.isRootNode();
        },
        getChildNodes() {
          return self.data.children || [];
        },
        canPaste() {
          const clipboard = self.context.clipboard;
          return clipboard && clipboard.node.data.nodeType == 'CONDITION';
        },
        copyNode() {
          self.copyNode();
        },
        cutNode() {
          self.cutNode();
        },
        pasteNode() {
          return self.pasteNode();
        },
        resize() {
          self.context.traverse();
        }
      }
    }));
  }
}
