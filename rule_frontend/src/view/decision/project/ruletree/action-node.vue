<template>
  <div class="tree-node" v-contextmenu="contextmenuData" v-if="!readonly">
    <div v-for="e,i in data.actions" :key="i">
      <action v-model="data.actions[i]"></action>
      <i class="el-icon-delete" style="cursor: pointer" @click="$delete(data.actions, i)"></i>
      <i class="el-icon-document-copy" style="cursor: pointer" @click="copyAction(i)"></i>
    </div>

    <template v-if="!data.actions || data.actions.length < 1">
      右键添加动作
    </template>
  </div>
  <div class="tree-node" v-else>
    <div v-for="e,i in data.actions" :key="i">
      <action v-model="data.actions[i]" :readonly="readonly"></action>
    </div>
  </div>
</template>

<script>
  export default {
    name: "action-node",
    props: {
      data: {
        type: Object,
        default: function () {
          return {actions: []}
        }
      },
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
              text: '添加动作',
              icon: 'el-icon-edit',
              command: {type: 'ADD'},
              click: this.operate
            },
            {
              text: '复制分支',
              icon: 'el-icon-document-copy',
              command: {type: 'COPY'},
              click: this.operate
            },
            {
              text: '剪切分支',
              icon: 'el-icon-scissors',
              command: {type: 'CUT'},
              click: this.operate
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
        if (command.type == 'ADD') {
          this.data.actions.push({})
        } else if (command.type == 'COPY') {
          this.copyNode()
        } else if (command.type == 'CUT') {
          this.cutNode()
        } else if (command.type == 'DELETE') {
          this.delete()
        }
      },
      copyAction(index) {
        const copy = this.$utils.copy(this.data.actions[index])
        this.data.actions.splice(index, 0, copy)
        // this.resizeNode()
      }
    }
  }
</script>

<style scoped>

</style>
