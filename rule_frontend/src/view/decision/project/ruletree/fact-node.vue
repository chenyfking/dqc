<template>
  <div class="tree-node">
    <div v-contextmenu="contextmenuData" v-if="!readonly">
      <fact v-model="data"></fact>
    </div>
    <fact v-else v-model="data" :readonly="readonly"></fact>

    <el-dialog
      title="按区间范围添加条件"
      :visible.sync="showSectionConditionDialog"
      @open="open"
      @closed="closed"
      v-if="!readonly"
      append-to-body>
      <div v-for="(e, i) in sections" :key="i" style="margin-bottom: 5px;">
        <el-input-number
          v-model="e.min"
          :controls="false"
          :max="getMax(i)"
          style="width: 100px;">
        </el-input-number>
        到
        <el-input-number
          v-model="e.max"
          :controls="false"
          :min="getMin(i)"
          style="width: 100px;">
        </el-input-number>
        <el-select v-model="e.type" style="width: 120px;">
          <el-option value="[]" label="闭区间 []"></el-option>
          <el-option value="()" label="开区间 ()"></el-option>
          <el-option value="[)" label="左闭右开 [)"></el-option>
          <el-option value="(]" label="左开右闭 (]"></el-option>
        </el-select>
        <el-button @click="delSection(i)">删除</el-button>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="showSectionConditionDialog = false" icon="el-icon-error">取 消</el-button>
        <el-button type="primary" @click="submitSectionCondition" icon="el-icon-success">确 定</el-button>
        <el-button type="primary" @click="addSection" icon="el-icon-plus">添加区间</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
  export default {
    name: "fact-node",
    props: {
      data: {
        type: Object,
        default: function () {
          return {}
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
          this.resetInput()
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
              click: this.operate
            },
            {
              text: '按区间范围添加条件',
              icon: 'el-icon-s-unfold',
              command: {type: 'SECTION_CONDITION'},
              click: this.operate,
              filter: () => {
                const id = this.data.id;
                const fieldId = this.data.fieldId;
                if (id && fieldId) {
                  const field = this.$store.getters.getFactField(id, fieldId);
                  if (this.$utils.isNumber(field.type)) {
                    return true;
                  }
                }
                return false;
              }
            },
            {
              text: 'ELSE分支',
              icon: 'el-icon-s-unfold',
              command: {type: 'ELSE_CONDITION'},
              click: this.operate,
              filter: () => {
                const children = this.getChildNodes();
                if (children.length > 0) {
                  let hasElse = false;
                  children.some(c => {
                    if (c.condition.other === true) {
                      hasElse = true;
                      return true;
                    }
                  });
                  return !hasElse;
                }
                return false;
              }
            },
            {
              text: '复制分支',
              icon: 'el-icon-document-copy',
              command: {type: 'COPY'},
              click: this.operate,
              filter: () => !this.isRootNode()
            },
            {
              text: '剪切分支',
              icon: 'el-icon-scissors',
              command: {type: 'CUT'},
              click: this.operate,
              filter: () => !this.isRootNode()
            },
            {
              text: '粘贴分支',
              icon: 'el-icon-document-copy',
              command: {type: 'PASTE'},
              click: this.operate,
              filter: () => this.canPaste()
            },
            {
              text: '删除节点',
              icon: 'el-icon-delete',
              command: {type: 'DELETE'},
              click: this.operate,
              divided: true
            }
          ]
        },
        showSectionConditionDialog: false,
        sections: []
      }
    },
    methods: {
      operate(command) {
        if (command.type == 'CONDITION') {
          this.addChild()
        } else if (command.type == 'SECTION_CONDITION') {
          this.showSectionConditionDialog = true
        } else if (command.type == 'ELSE_CONDITION') {
          this.addChild({other: true})
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
      submitSectionCondition() {
        this.sections.forEach(e => {
          if (this.$utils.isNotBlank(e.min) && this.$utils.isNotBlank(e.max)) {
            let minNode
            if (e.type.startsWith('[')) {
              minNode = this.addChild({op: 'GTE', right: {type: 'INPUT', input: {value: e.min, type: 'Double'}}})
            } else {
              minNode = this.addChild({op: 'GT', right: {type: 'INPUT', input: {value: e.min, type: 'Double'}}})
            }
            if (e.type.endsWith(']')) {
              minNode.vm.addChild('CONDITION', {op: 'LTE', right: {type: 'INPUT', input: {value: e.max, type: 'Double'}}})
            } else {
              minNode.vm.addChild('CONDITION', {op: 'LT', right: {type: 'INPUT', input: {value: e.max, type: 'Double'}}})
            }
          } else if (this.$utils.isNotBlank(e.min)) {
            const data = {right: {type: 'INPUT', input: {value: e.min, type: 'Double'}}};
            if (e.type.startsWith('[')) {
              data.op = 'GTE';
            } else {
              data.op = 'GT';
            }
            this.addChild(data);
          } else if (this.$utils.isNotBlank(e.max)) {
            const data = {right: {type: 'INPUT', input: {value: e.max, type: 'Double'}}};
            if (e.type.endsWith(']')) {
              data.op = 'LTE';
            } else {
              data.op = 'LT';
            }
            this.addChild(data);
          }
        })
        this.showSectionConditionDialog = false
      },
      addSection() {
        this.sections.push({type: "[]"})
      },
      open() {
        this.addSection()
      },
      closed() {
        this.sections = []
      },
      delSection(i) {
        this.sections.splice(i, 1)
      },
      getMin(i) {
        const section = this.sections[i]
        return section.min || -Infinity
      },
      getMax(i) {
        const section = this.sections[i]
        return section.max || Infinity
      }
    }
  }
</script>

<style scoped>

</style>
