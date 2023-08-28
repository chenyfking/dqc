<template>
  <div>
    <el-row v-if="!readonly">
      <el-col class="toolbar">
        <div class="pin">
          <save-btn @callback="saveCallback" :assets="assets" @refresh="refresh" @template="ruletreeTemplate"></save-btn>
          <test-btn :assets="assets"></test-btn>
          <import-btn :assets="assets" @importTpl="importTpl"></import-btn>
          <refer-btn :assets="assets"></refer-btn>
          <assets-validation :assets="assets"></assets-validation>
          <assets-editor-dialog :assets="assets" @refresh="refresh"></assets-editor-dialog>
          <delete-btn :assets="assets" @delete="uuid => {$emit('delete', uuid)}"></delete-btn>
        </div>
      </el-col>
    </el-row>

    <el-row style="overflow-x:auto;" ref="treeContent">
      <el-col>
        <el-button
          type="text"
          v-if="$hasPerm('project.asset:edit') && !readonly"
          @click="showAttrDialog = true">
          设置属性
        </el-button>
        <el-tag
          v-for="(attr, index) in attrs"
          :key="attr.name"
          size="small"
          type="warning"
          :closable="!readonly"
          @close="attrs.splice(index, 1)"
          style="margin-right: 10px;">
          {{attr.label}}： {{attr.text}}
        </el-tag>
        <el-switch
          v-if="fireRules.length > 0"
          v-model="showFireRules"
          active-text="仅显示触发规则"
          @change="filterRules"
          style="margin-bottom: 10px;">
        </el-switch>
        <div id="treeContainer" ref="treeContainer"></div>
      </el-col>
    </el-row>

    <attr :show.sync="showAttrDialog" :attrs.sync="attrs"></attr>
  </div>
</template>

<script>
import Context from "./context";
import deleteBtn from "../assets/delete-btn";
import referBtn from "../assets/assets-refer-btn";
import importBtn from "../assets/assets-import-template";
import testBtn from "../assets/test-btn";
import assetsValidation from "../assets/assets-validation-rules";

export default {
  name: "index",
  components: {
    "delete-btn": deleteBtn,
    "test-btn": testBtn,
    "refer-btn": referBtn,
    "import-btn": importBtn,
    "assets-validation": assetsValidation
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
    assets: {
      deep: true,
      handler: function() {
        // this.initTree();
      }
    }
  },
  data() {
    return {
      attrs: [],
      tree: {
        children: []
      },
      showAttrDialog: false,
      showFireRules: true,
      originalTree: null,
      stack: [],
      branchNum: 0,
      context: null
    };
  },
  mounted() {
    this.initTree()
  },
  methods: {
    initTree() {
      if (this.assets.content) {
        const json = JSON.parse(this.assets.content);
        if (json.tree) {
          this.attrs = json.attrs;
          this.originalTree = json.tree
          this.filterRules()
        }
      } else {
        this.attrs = [];
        this.tree = {
          fact: {},
          children: []
        }
        this.renderTree()
      }
    },
    importTpl(tplAssets) {
      this.assets.content = tplAssets
      this.initTree()
    },
    saveCallback(callback) {
      callback(JSON.stringify({
        attrs: this.attrs,
        tree: this.tree
      }))
    },
    ruletreeTemplate(){
      this.$emit('addTemplate')
    },
    refresh(assets) {
      if (assets.changeVersion === true) {
        const json = JSON.parse(assets.content)
        if (json.tree) {
          this.attrs = json.attrs
          this.tree = json.tree
        } else {
          this.attrs = []
          this.tree = {
            fact: {},
            children: []
          }
        }
        this.renderTree()
      } else {
        this.$emit('refresh', assets)
      }
    },
    renderTree() {
      this.$nextTick(() => {
        const context = new Context(this.$refs.treeContainer);
        context.readonly = this.readonly;
        context.initTree(this.tree);
        this.context = context;
      })
    },
    filterRules() {
      if (this.fireRules.length == 0 || !this.showFireRules) {
        this.tree.fact = this.originalTree.fact
        this.tree.children = this.originalTree.children
        this.renderTree()
        return
      }

      this.stack = []
      this.branchNum = 0
      this.tree.fact = this.originalTree.fact
      this.tree.children = []

      this.traversal(this.originalTree)
      this.renderTree()
    },
    traversal(node) {
      this.pushStack(node)

      if (!node.children || node.children.length == 0) {
        this.handleStack()
        return
      }

      node.children.forEach(child => {
        child._parent = node
        this.traversal(child)
      })
    },
    pushStack(node) {
      if (this.stack.length == 0) {
        this.stack.push(node)
        return
      }

      while (this.stack[this.stack.length - 1] != node._parent) {
        this.stack.pop()
      }
      this.stack.push(node)
    },
    handleStack() {
      this.branchNum++

      this.fireRules.forEach(rule => {
        const rowNum = this.assets.versionNo
          ? parseInt(rule.substring(`决策树-${this.assets.name}_V${this.assets.versionNo}-`.length))
          : parseInt(rule.substring(`决策树-${this.assets.name}-`.length))
        if (rowNum == this.branchNum) {
          let parent = this.tree
          this.stack.forEach((e, i) => {
            if (i > 0) {
              const node = {}
              Object.keys(e).forEach(k => {
                if (k != 'children' && !k.startsWith('_')) {
                  node[k] = e[k]
                }
              })

              if (i < this.stack.length - 1) {
                node.children = []
              }

              parent.children.push(node)
              parent = node
            }
          })
        }
      })
    }
  },
  destroyed() {
    this.context.destroy()
  }
};
</script>

<style scoped>
  #treeContainer {
    position: relative;
    text-align: center;
    overflow-y: hidden;
  }
</style>
