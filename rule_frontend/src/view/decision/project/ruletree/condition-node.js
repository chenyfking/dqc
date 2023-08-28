import Vue from 'vue'
import Node from './node'
import FactNode from './fact-node'
import ConditionNodeVue from './condition-node.vue'
import store from '../../../../vuex/index'

const VueConstructor = Vue.extend(ConditionNodeVue);

export default class ConditionNode extends Node {
  constructor(context, parentNode, data) {
    super(context, parentNode, data);
    this.init();
  }

  init() {
    const self = this;
    super.mount(new VueConstructor({
      store,
      propsData: {
        data: self.data.condition,
        inputType: self.getInputType()
      },
      methods: {
        addChild(nodeType, data) {
          let childData = {nodeType: nodeType}
          if (nodeType == 'CONDITION') {
            childData.condition = data || {}
          } else if (nodeType == 'ACTION') {
            childData.rhs = data || {actions: [{}]}
          } else if (nodeType == 'FACT') {
            childData.fact = {}
          }
          if (!self.data.children) {
            self.data.children = []
          }
          self.data.children.push(childData);
          return self.addChild(childData);
        },
        delete() {
          self.delete();
        },
        repeatCondition() {
          self.repeatNode();
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
        getClipboard() {
          return self.context.clipboard;
        },
        resize() {
          self.context.traverse();
        }
      }
    }));
  }

  initInputType() {
    this.vm.inputType = this.getInputType()
  }

  getInputType() {
    let inputType;
    const factNode = this.findFactParentNode();
    const fact = factNode.data.fact;
    if (fact && fact.id) {
      const field = store.getters.getFactField(fact.id, fact.fieldId);
      if (field) {
        inputType = field.type;
      }
    }
    return inputType;
  }

  findFactParentNode() {
    if (this.parentNode instanceof FactNode) {
      return this.parentNode;
    }
    return this.parentNode.findFactParentNode();
  }
}
