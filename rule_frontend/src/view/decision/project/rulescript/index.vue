<template>
  <div>
    <el-row v-if="!readonly">
      <el-col class="toolbar">
        <div class="pin">
          <save-btn @callback="saveCallback" :assets="assets" @refresh="refresh" ref="saveBtn"></save-btn>
          <test-btn :assets="assets"></test-btn>
          <refer-btn :assets="assets"></refer-btn>
          <assets-editor-dialog :assets="assets" @refresh="refresh"></assets-editor-dialog>
          <delete-btn :assets="assets" @delete="uuid => {$emit('delete', uuid)}"></delete-btn>
        </div>
      </el-col>
    </el-row>

    <el-row>
      <el-container>
        <el-aside v-if="!readonly" width="250px" style="background: #fff; overflow-y: hidden;">
          <el-tree
            class="rulescriptTree"
            :data="bomNodes"
            node-key="id"
            @node-click="handleNodeClick"
            :expand-on-click-node="false"
            :default-expanded-keys="defaultExpandedKeys"
            ref="tree">
          </el-tree>
        </el-aside>
        <el-main ref="main" style="overflow: hidden;" v-loading="editorLoading" element-loading-text="初始化编辑框...">
          <el-switch
            v-if="fireRules.length > 0"
            v-model="showFireRules"
            active-text="仅显示触发规则"
            @change="filterRules"
            style="margin-bottom: 10px;">
          </el-switch>
          <div ref="editor" name="fieldName" style="height: 400px;"></div>
        </el-main>
      </el-container>
    </el-row>
  </div>
</template>
<script>
import deleteBtn from "../assets/delete-btn";
import testBtn from "../assets/test-btn";
import referBtn from "../assets/assets-refer-btn";

export default {
  name: "index",
  components: {
    "delete-btn": deleteBtn,
    "test-btn": testBtn,
    "refer-btn": referBtn
  },
  data() {
    return {
      editor: null,
      showAddFunDialog: false,
      bomNodes: [],
      defaultExpandedKeys: [],
      script: '',
      showFireRules: true,
      editorLoading: false
    };
  },
  props: {
    assets: Object,
    readonly: {
      type: Boolean,
      default: false
    },
    fireRules: {
      type: Array,
      default: function() {
        return []
      }
    }
  },
  watch: {
    readonly: function(newVal, oldVal) {
      if (oldVal === true && newVal !== true) {
        // 解锁文件
        this.$nextTick(() => {
          this.editor.updateOptions({
            readOnly:false
          });
        })
      }
    }
  },
  mounted() {
    this.initData();
    setTimeout(() => {
      this.initEditor();
    }, 0);
    this.loadBomNodes()
  },
  methods: {
    loadBomNodes() {
      const factRoot = {
        id: 'fact',
        label: '数据结构',
        children: []
      }
      const thirdapisRoot = {
        id: 'thirdapis',
        label: '外部接口',
        children: []
      }
      this.defaultExpandedKeys.push(factRoot.id)
      this.$store.state.facts.forEach((fact, i) => {
        const factNode = {
          id: fact.uuid,
          label: fact.name + '(' + fact.id + ')',
          type: 'fact',
          data: fact,
          children: []
        }
        fact.fields.forEach(field => {
          if (field.type != 'Derive') {
            let label = field.label
            if (label != field.name) {
              label += '(' + field.name + ')'
            }
            factNode.children.push({
              id: field.id,
              label: label,
              type: 'factField',
              data: field
            })
          }
        })
        factRoot.children.push(factNode)
        if (i === 0) {
          this.defaultExpandedKeys.push(factNode.id)
        }
      })
      this.$store.state.thirdapis.forEach((api,index)=>{
        const apiNode = {
          id: api.uuid,
          label: api.name,
          type: 'api',
          data: api,
        }
        thirdapisRoot.children.push(apiNode)
      })
      this.bomNodes.push(factRoot)
      this.bomNodes.push(thirdapisRoot)
    },
    async initEditor() {
      const self = this
      try {
        this.editorLoading = true;
        const monaco = await import('monaco-editor/esm/vs/editor/editor.api');
        const editor = monaco.editor.create(this.$refs.editor, {
          readOnly: this.readonly,
          language: "java"
        });

        editor.addCommand(monaco.KeyMod.CtrlCmd | monaco.KeyCode.KEY_S, function() {
          const content = JSON.stringify({script: self.editor.getValue()})
          self.$refs.saveBtn.saveContent(content)
        });
        this.editorLoading = false;

        this.editor = editor;

        this.filterRules();
      } catch (e) {
        this.editorLoading = false;
        this.$message.error('初始化编辑框失败');
      }
    },
    initData() {
      if (this.assets.content) {
        const json = JSON.parse(this.assets.content)
        this.script = json.script
      } else {
        this.script = 'rule "规则1"\n    when\n\n    then\n\nend'
      }
    },
    saveCallback(callback) {
      callback(JSON.stringify({script: this.editor.getValue()}));
    },
    handleNodeClick(data) {
      if (!data.data) return

      let insertContent;
      if (data.type == 'fact') {
        insertContent = data.data.import + '()';
      } else if (data.type == 'factField') {
        insertContent = data.data.name;
      }else if(data.type == 'api'){
        insertContent =`外部接口:(${data.data.name},输入,输出)`
      }

      if (!insertContent) return;

      const selection = this.editor.getSelection();
      const id = { major: 1, minor: 1 };
      const op = {identifier: id, range: selection, text: insertContent, forceMoveMarkers: true};
      this.editor.executeEdits(null, [op]);
    },
    refresh(assets) {
      if (assets.changeVersion === true) {
        const json = JSON.parse(assets.content)
        this.script = json.script
        this.editor.setValue(this.script)
      } else {
        this.$emit('refresh',assets)
      }
    },
    filterRules() {
      if (this.fireRules.length == 0 || !this.showFireRules) {
        this.editor.setValue(this.script)
        return
      }

      let ruleArr = []
      this.fireRules.forEach(rule => {
        const ruleName = this.assets.versionNo
          ? rule.substring(`脚本式决策集-${this.assets.name}_V${this.assets.versionNo}-`.length)
          : rule.substring(`脚本式决策集-${this.assets.name}-`.length)
        const reg = new RegExp(`rule\\s+?"${ruleName}"[\\s\\S]+?end`, 'g')
        const result = this.script.match(reg)
        if (result && result.length > 0) {
          ruleArr.push(...result)
        }
      })
      this.editor.setValue(ruleArr.join('\n\n'))
    }
  },
  beforeDestroy() {
    this.editor != null && this.editor.dispose();
  }
};
</script>

<style scoped>
</style>
