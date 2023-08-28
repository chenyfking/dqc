<template>
  <div class="tree-node" v-contextmenu="contextmenuData" v-if="!readonly" >
    <template v-if="!data.other">
      <op v-model="data.op" v-if="inputType" :input-type="inputType" :key="Math.random()"></op>
      <expression v-model="data.right" v-if="inputType" :input-type="getRightInputType()"></expression>
      <template v-if="!inputType">
         -
      </template>
    </template>
    <template v-else>
      ELSE分支
    </template>
  </div>
  <div class="tree-node" v-else>
    <template v-if="!data.other">
      <op v-model="data.op" v-if="inputType" :input-type="inputType" :key="Math.random()" readonly></op>
      <expression v-model="data.right" v-if="inputType" :input-type="getRightInputType()" readonly></expression>
      <template v-if="!inputType">
        -
      </template>
    </template>
    <template v-else>
      ELSE分支
    </template>
  </div>
</template>

<script>
  export default {
    name: "condition-node",
    props: {
      data: {
        type: Object,
        default: function () {
          return {}
        }
      },
      inputType: String,
      readonly: {
        type: Boolean,
        default: false
      }
    },
    watch: {
      data: {
        deep: true,
        handler: function() {
          this.resize();
        }
      }
    },
    data() {
      return {
        contextmenuData: {
          items: [
            {
              text: '添加条件',
              icon: 'el-icon-s-unfold',
              command: {type: 'CONDITION'},
              click: this.operate,
              filter: () => this.data.other !== true
            },
            {
              text: '复制条件',
              icon: 'el-icon-document-copy',
              command: {type: 'REPEAT'},
              click: this.operate,
              filter: () => this.data.other !== true
            },
            {
              text: '添加动作',
              icon: 'el-icon-s-tools',
              command: {type: 'ACTION'},
              click: this.operate
            },
            {
              text: '添加模型',
              icon: 'iconfont iconshujumoxing',
              command: {type: 'FACT'},
              click: this.operate
            },
            {
              text: '复制分支',
              icon: 'el-icon-document-copy',
              command: {type: 'COPY'},
              click: this.operate,
              filter: () => this.data.other !== true
            },
            {
              text: '剪切分支',
              icon: 'el-icon-scissors',
              command: {type: 'CUT'},
              click: this.operate,
              filter: () => this.data.other !== true
            },
            {
              text: '粘贴分支',
              icon: 'el-icon-document-copy',
              command: {type: 'PASTE'},
              click: this.operate,
              filter: () => {
                const clipboard = this.getClipboard()
                if (!clipboard) {
                  return false;
                } else if (this.data.other === true && clipboard.node.data.nodeType == 'CONDITION') {
                  return false;
                }
                return true;
              }
            },
            {
              text: '删除节点',
              icon: 'el-icon-delete',
              command: {type: 'DELETE'},
              click: this.operate,
              divided: true
            }
          ]
        }
      }
    },
    methods: {
      operate(command) {
        if (command.type == 'CONDITION') {
          this.addChild(command.type)
        } else if (command.type == 'REPEAT') {
          this.repeatCondition()
        } else if (command.type == 'ACTION') {
          this.addChild('ACTION')
        } else if (command.type == 'FACT') {
          this.addChild('FACT')
        } else if (command.type == 'COPY') {
          this.copyNode()
        } else if (command.type == 'CUT') {
          this.cutNode()
        } else if (command.type == 'PASTE') {
          const msg = this.pasteNode()
          if (msg) this.$message.warning(msg)
        } else if (command.type == 'DELETE') {
          this.delete()
        }
      },
      getRightInputType() {
        if (this.data.op === '字符串长度为' || this.data.op === '字符串长度不为') {
          return 'Integer'
        } else {
          return this.inputType
        }
      }
    }
  }
</script>

<style scoped>

</style>
