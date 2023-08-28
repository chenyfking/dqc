<template>
  <div>
    <el-row v-if="!readonly">
      <el-col class="toolbar">
        <div class="pin">
          <save-btn
            :assets="assets"
            @callback="saveCallback"
            @refresh="refresh"
            @template="guidedTemplate">
          </save-btn>
          <test-btn :assets="assets" v-if="!assets.isTemplate"></test-btn>
          <dropdown
            split-button
            @click="addRule"
            v-if="$hasPerm('project.asset:edit')"          >
            <i class="el-icon-plus"></i> 添加规则
            <dropdown-menu slot="dropdown">
              <dropdown-item @click.native="addRule(true)" icon="el-icon-refresh">添加循环规则</dropdown-item>
            </dropdown-menu>
          </dropdown>
          <import-btn :assets="assets" @importTpl="importTpl"></import-btn>
          <refer-btn :assets="assets"></refer-btn>
          <assets-validation :assets="assets"></assets-validation>
          <assets-editor-dialog :assets="assets" @refresh="refresh"></assets-editor-dialog>
          <!-- <delete-btn :assets="assets" @delete="uuid => {$emit('delete', uuid)}"></delete-btn> -->
        </div>
      </el-col>
    </el-row>

    <el-row>
      <el-col class="rule-container">
        <el-switch
          v-if="fireRules.length > 0"
          v-model="showFireRules"
          active-text="仅显示触发规则"
          @change="filterRules"
          style="margin-bottom: 10px;">
        </el-switch>
        <el-card
          v-for="(rule, index) in rules"
          :key="rule._id ? rule._id : (rule._id = $utils.randomCode(6))"
          shadow="hover"
          :class="rule.cssStyle"
          style="overflow-x: auto">
          <div slot="header" class="headerContent">
            <div class="editValue">
              <el-popover @after-enter="openNameInput(index)" v-if="!readonly">
                <el-input
                  v-model.trim="rule.name"
                  :ref="'name_' + index"
                  @change="val => editName(index, val)">
                </el-input>
                <el-link class="rule-name" :underline="false" slot="reference">{{rule.name}}</el-link>
              </el-popover>
              <span v-else>{{rule.name}}</span>
              <div class="tagList">
                <el-tag
                  v-for="(attr, index) in rule.attrs"
                  :key="attr.name"
                  size="small"
                  type="warning"
                  :closable="!readonly"
                  @close="rule.attrs.splice(index, 1)"
                  style="margin-right: 10px;">
                  {{attr.label}}： {{attr.text}}
                </el-tag>
              </div>
            </div>
            <div v-if="$hasPerm('project.asset:edit') && !readonly">
              <el-tooltip content="属性" placement="top">
                <i class="el-icon-set-up icon-btn rule-btn" @click="setAttr(index)"></i>
              </el-tooltip>
              <el-tooltip content="删除" placement="top">
                <div style="float: right">
                  <el-popconfirm title="确定删除当前规则？" @confirm="rules.splice(index, 1)">
                    <i class="el-icon-delete icon-btn rule-btn" slot="reference"></i>
                  </el-popconfirm>
                </div>
              </el-tooltip>
              <el-tooltip content="上移" placement="top">
                <i class="el-icon-top icon-btn rule-btn" @click="moveUp(index)"></i>
              </el-tooltip>
              <el-tooltip content="下移" placement="top">
                <i class="el-icon-bottom icon-btn rule-btn" @click="moveDown(index)"></i>
              </el-tooltip>
              <el-tooltip content="复制" placement="top">
                <i class="el-icon-document-copy icon-btn rule-btn" @click="copyRule(index)"></i>
              </el-tooltip>
            </div>
          </div>
          <div class="rule-body">
            <p v-if="rule.loop">
              循环对象:
              <fact v-model="rule.loopTarget" input-type="Collection" :readonly="readonly"></fact>
            </p>
            <p>如果</p>
            <lhs v-model="rule.lhs" :loop-target="rule.loopTarget" :readonly="readonly"></lhs>
            <p>那么</p>
            <rhs v-model="rule.rhs" :loop-target="rule.loopTarget" :readonly="readonly"></rhs>
            <p style="margin-top: 5px;">否则</p>
            <rhs v-model="rule.other" :loop-target="rule.loopTarget" :readonly="readonly"></rhs>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <attr
      v-if="rules.length > 0"
      :show.sync="showAttrDialog"
      :attrs.sync="rules[editRuleIndex].attrs">
    </attr>
  </div>
</template>

<script>
import lhs from "../components/lhs";
import rhs from "../components/rhs";
import deleteBtn from "../assets/delete-btn";
import referBtn from "../assets/assets-refer-btn";
import importBtn from "../assets/assets-import-template";
import testBtn from "../assets/test-btn";
import assetsValidation from "../assets/assets-validation-rules";

let _rules

export default {
  name: "index",
  components: {
    lhs,
    rhs,
    "delete-btn": deleteBtn,
    "refer-btn": referBtn,
    "import-btn": importBtn,
    "test-btn": testBtn,
    'assets-validation': assetsValidation,
  },
  data() {
    return {
      rules: [],
      showAttrDialog: false,
      editRuleIndex: 0,
      saveLoading: false,
      saveVersionLoading: false,
      showNewVersionDialog: false,
      nowVersion: [],
      targetVersion: [],
      showFireRules: true
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
  mounted() {
    this.initData();
  },
  methods: {
    initData() {
      if (this.assets.content) {
        const json = JSON.parse(this.assets.content);
        if (json.rules) {
          _rules = json.rules
          this.filterRules()
        }
      } else {
        this.rules = [];
      }
      if (this.rules.length < 1) {
        this.addRule();
      }
    },
    addRule(loop) {
      const rule = {
        name: `规则${this.rules.length + 1}`,
        attrs: []
      };
      if (loop === true) rule.loop = loop;
      this.rules.push(rule);
    },
    importTpl(tplAssets){
      this.assets.content = tplAssets
      this.initData()
    },
    setAttr(index) {
      this.editRuleIndex = index;
      this.showAttrDialog = true;
    },
    moveUp(index) {
      if (index > 0) {
        this.swapRule(index, index - 1);
      } else if (index <= 0) {
        this.$message({
          message: '最顶部规则不可上移',
          type: 'warning'
        });
      }
    },
    moveDown(index) {
      if (index < this.rules.length - 1) {
        this.swapRule(index, index + 1);
      }
    },
    copyRule(index) {
      let rule = JSON.parse(JSON.stringify(this.rules[index]));
      this.$utils.clearTmpKey(rule);
      rule.name = `规则${this.rules.length + 1}`;
      this.rules.splice(index + 1, 0, rule);
    },
    swapRule(index1, index2) {
      let rules = [];
      this.rules.forEach(e => {
        rules.push(e);
      });
      const tmp = rules[index1];
      rules[index1] = rules[index2];
      rules[index2] = tmp;
      this.rules = rules;
    },
    openNameInput(index) {
      this.$refs["name_" + index][0].focus();
    },
    editName(index, name) {
      if (!name) {
        this.rules[index].name = "规则" + (index + 1);
        return;
      }

      let exists = false;
      this.rules.some((rule, i) => {
        if (rule.name == name && index != i) {
          exists = true;
          return true;
        }
      });
      if (exists) {
        this.rules[index].name = "规则" + (index + 1);
        this.$message.warning("规则不能重名");
        return;
      }
    },
    saveCallback(callback) {
      const rules = JSON.parse(JSON.stringify(this.rules));
      this.$utils.clearTmpKey(rules);
      callback(JSON.stringify({ rules: rules }));
    },
    guidedTemplate() {
      this.$emit('addTemplate')
    },
    refresh(assets) {
      if (assets.changeVersion === true) {
        const json = JSON.parse(assets.content)
        if (json.rules) {
          this.rules = json.rules
        } else {
          this.rules = []
        }
      } else {
        this.$emit('refresh', assets)
      }
    },
    filterRules() {
      if (this.fireRules.length == 0 || !this.showFireRules) {
        this.rules = _rules
        return
      }

      this.rules = _rules.filter(rule => {
        const prefix = this.assets.versionNo
          ? `向导式决策集-${this.assets.name}_V${this.assets.versionNo}-${rule.name}`
          : `向导式决策集-${this.assets.name}-${rule.name}`
        return this.fireRules.indexOf(prefix) != -1
          || this.fireRules.indexOf(`${prefix}-ELSE分支`) != -1
      })
    }
  }
};
</script>

<style scoped>
  .rule-body {
    line-height: 20px;
  }
  .el-card {
    margin-bottom: 10px;
  }
  .rule-name {
    padding: 10px 0px;
  }
  .rule-btn {
    float: right;
    padding: 10px 0px;
  }
  .headerContent {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  .editValue {
    display: flex;
    align-items: center;
  }
  .editValue .tagList {
    margin-left: 12px;
  }
</style>
