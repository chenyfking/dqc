<template>
  <div class="testcase">
    <div class="toolbar" v-if="!readonly">
      <el-button
        type="primary"
        :loading="saveLoading"
        @click="saveSubmitTest"
        icon="iconfont iconbaocun">
        {{saveLoading ? '保存中...' : '保存'}}
      </el-button>
      <el-button
        :loading="testLoading"
        @click="() => !testLoading && submitTest()"
        icon="el-icon-lightning">
        {{testLoading ? '测试中...' : '测试'}}
      </el-button>
      <assets-editor-dialog :assets="assets" @refresh="refresh"></assets-editor-dialog>
      <el-popconfirm title="确定删除？" @onConfirm="deleData">
        <el-button slot="reference" icon="el-icon-delete">删除</el-button>
      </el-popconfirm>
    </div>
    <div class="addFile">
      <span class="title">已选中：</span>
      <span v-if="!readonly" class="selectAsset" @click="showAssetsAddDialog = true">
        {{fileName ? fileName : '请选择'}}
      </span>
      <span v-else>
        {{fileName ? fileName : '请选择'}}
      </span>
    </div>
    <el-alert
      v-show="testData.result"
      class="toolbar"
      title="测试成功"
      :closable="false"
      type="success"
      show-icon>
      <p>
        触发了<b>{{testResult.fireNum}}</b>条规则，
        耗时<b>{{testResult.fireTime}}</b>毫秒
      </p>
      <template v-if="testResult.fireNodes && testResult.fireNodes.length > 0">
        <p>决策流轨迹：</p>
        <p v-for="e, i in testResult.fireNodes" :key="e.processId" style="margin-left: 20px;">
          <el-link
            type="success"
            :underline="false"
            @click="showFlow(i)"
            icon="el-icon-view">
            {{e.flowName}}
          </el-link>
        </p>
      </template>
      <template v-if="testResult.fireNum > 0">
        <p>规则列表：</p>
        <p v-for="e, i in testResult.fireRules" :key="e.name" style="margin-left: 20px;">
          <el-link
            type="success"
            :underline="false"
            @click="showAssets(i)"
            icon="el-icon-view">
            {{e.name}}
          </el-link>
        </p>
      </template>
    </el-alert>

    <el-row v-loading="testDataLoading" :gutter="10">
      <el-col :span="6">
        <el-table
          :data="testData.facts"
          :row-style="{cursor: 'pointer'}"
          :row-class-name="setRowIndex"
          @row-click="clickTestFact"
          highlight-current-row
          border
          ref="modelTable">
          <el-table-column prop="name" label="数据结构" align="center"></el-table-column>
        </el-table>
      </el-col>
      <el-col :span="18">
        <el-table
          v-for="(fact, index) in testData.facts"
          :key="fact.id"
          @cell-click="cellClick"
          v-show="index == testData.activeIndex"
          :data="fact.fields"
          :ref="'singleTable'+index"
          :row-style="{cursor: 'pointer'}"
          class="tb-edit"
          highlight-current-row
          border>
          <el-table-column label="值" align="center">
            <template slot-scope="scope">
              <el-input-number
                v-if="$utils.isNumber(scope.row.type) && !readonly"
                v-model="scope.row.original"
                :ref="'input_original_' + scope.row.id"
                style="width: 100%;"
                :step-strictly="inputInteger(scope.row)"
                :controls="false">
              </el-input-number>
              <el-select
                v-else-if="$utils.isBoolean(scope.row.type) && !readonly"
                v-model="scope.row.original"
                :ref="'input_original_' + scope.row.id"
                placeholder="请选择">
                <el-option value="true" label="是"></el-option>
                <el-option value="false" label="否"></el-option>
              </el-select>
              <el-date-picker
                v-else-if="$utils.isDate(scope.row.type) && !readonly"
                v-model="scope.row.original"
                type="datetime"
                :ref="'input_original_' + scope.row.id"
                value-format="yyyy-MM-dd HH:mm:ss"
                placeholder="请选择"
                align="center"
                style="width: 100%;">
              </el-date-picker>
              <test-collection
                v-else-if="$utils.isCollection(scope.row.type) && !readonly"
                v-model="scope.row.original"
                :ref="'input_original_' + scope.row.id"
                :type="scope.row.subType">
              </test-collection>
              <test-object
                v-else-if="$utils.isObject(scope.row.type) && !readonly"
                v-model="scope.row.original"
                :ref="'input_original_' + scope.row.id">
              </test-object>
              <el-input
                v-model.trim="scope.row.original"
                v-else-if="!readonly"
                :ref="'input_original_' + scope.row.id">
              </el-input>
              <span v-if="$utils.isBoolean(scope.row.type)">
                {{!scope.row.original ? '' : (scope.row.original === 'true' ? '是' : '否')}}
              </span>
              <span v-else-if="!$utils.isCollection(scope.row.type) && !$utils.isObject(scope.row.type)">
                {{scope.row.original}}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="期望值" align="center">
            <template slot-scope="scope">
              <el-input
                v-if="!readonly"
                v-model.trim="scope.row.expectedValue"
                :ref="'input_expectedValue_' + scope.row.id"
              ></el-input>
              <span
                v-if="$utils.isBoolean(scope.row.type)"
                :class="[scope.row.expectedValue!=(!scope.row.value ? '' : (scope.row.value === 'true' ? '是' : '否')) ? 'activeClass' : '']"
              >{{scope.row.expectedValue}}</span>
              <span
                v-else
                :class="[scope.row.expectedValue!=scope.row.value? 'activeClass' : '']"
              >{{scope.row.expectedValue}}</span>
            </template>
          </el-table-column>
          <el-table-column label="结果" align="center">
            <template slot-scope="scope">
              <span
                v-if="$utils.isBoolean(scope.row.type)"
                :class="[scope.row.expectedValue!==(!scope.row.value ? '' : (scope.row.value === 'true' ? '是' : '否')) ? 'activeClass' : '']"
              >{{!scope.row.value ? '' : (scope.row.value === 'true' ? '是' : '否')}}</span>
              <el-popover
                v-else-if="$utils.isObject(scope.row.type) && scope.row.value"
                placement="right"
                width="400"
                trigger="click"
              >
                <span
                  :class="[scope.row.expectedValue!=scope.row.value ? 'activeClass' : '']"
                >{{scope.row.value}}</span>
                <el-link icon="el-icon-view" slot="reference"></el-link>
              </el-popover>
              <span
                v-else
                :class="[scope.row.expectedValue!=scope.row.value ? 'activeClass' : '']"
              >{{scope.row.value}}</span>
            </template>
          </el-table-column>
          <el-table-column prop="label" label="中文名称" align="center"></el-table-column>
          <el-table-column label="数据类型" align="center">
            <template slot-scope="scope">
              <span
                v-if="$utils.isCollection(scope.row.type)"
              >{{scope.row.type + '<' + getSubTypeName(scope.row.subType) + '>'}}</span>
              <span
                v-else-if="$utils.isObject(scope.row.type)"
              >{{getObjectTypeName(scope.row.subType)}}</span>
              <span v-else>{{scope.row.type}}</span>
            </template>
          </el-table-column>
        </el-table>
      </el-col>
    </el-row>

    <ruleflow-trace
      :flow-uuid="ruleflowTrace.uuid"
      :flow-version="ruleflowTrace.version"
      :nodes="ruleflowTrace.nodes"
      :show.sync="ruleflowTrace.show">
    </ruleflow-trace>
    <assets-view
      :assets-uuid="viewingAssets.uuid"
      :assets-version="viewingAssets.version"
      :show.sync="viewingAssets.show">
    </assets-view>
    <assets-add-dialog
      :active.sync="showAssetsAddDialog"
      :assets="[{uuid: ruleUuid}]"
      :filter-exists="false"
      @add="submitAddAssets">
    </assets-add-dialog>
  </div>
</template>
<script>
import assetsAddDialog from "../knowledgepackage/assets-add-dialog";
export default {
  name: "test-dialog",
  props: {
    assets: Object,
    active: Boolean,
    temp: Boolean,
    readonly: {
      type: Boolean,
      default: false
    }
  },
  components: {
    "assets-add-dialog": assetsAddDialog
  },
  watch: {
    active: function(newVal) {
      this.show = newVal;
    }
  },
  data() {
    return {
      show: this.active,
      testLoading: false,
      saveLoading: false,
      testDataLoading: false,
      testData: {
        activeIndex: 0,
        facts: []
      },
      showBatchDialog: false,
      batchLoading: false,
      batchData: {
        activeIndex: 0,
        facts: null
      },
      batchTest: [],
      lastSubmitFacts: null,
      testResult: {},
      showAssetsAddDialog: false,
      fileName: "",
      ruleUuid: "",
      pkg: {},
      ruleflowTrace: {
        uuid: '',
        version: 0,
        nodes: [],
        show: false
      },
      viewingAssets: {
        uuid: '',
        version: 0,
        show: false
      },
      uuids:""
    };
  },
  mounted() {
    this.initData();
    if (!this.assets.content) {
      this.temptest()
    }
  },
  methods: {
    initData() {
      if (this.assets.content) {
        this.testData = JSON.parse(this.assets.content);
        this.fileName = this.testData.fileData.fileName;
        this.ruleUuid = this.testData.fileData.ruleUuid;
        this.assetsVersion = this.testData.fileData.assetsVersion;
        this.testData.result = null;
        this.pkg = this.testData.fileData.pkg
      }
    },
    temptest() {
      if (!this.ruleUuid) return;
      this.loading = true;
      this.$axios
        .post(
          "/knowledgepackage/temptest",
          this.$qs.stringify({
            assetsUuid: this.ruleUuid,
            assetsVersion: this.assetsVersion
          })
        )
        .then(res => {
          this.loading = false;
          if (res.data.code == 0) {
            this.$store.commit("setPkg", res.data.data);
            this.pkg = res.data.data
            this.open(res.data.data.uuid);
          } else {
            this.$message.error(res.data.msg ? res.data.msg : "操作失败")
          }
        })
        .catch(() => {
          this.loading = false;
        });
    },
    open(uuid) {
      this.testDataLoading = true;
      this.testData.result = null;
      this.lastSubmitFacts = null;
      this.$axios
        .get("/knowledgepackage/testdata", {
          params: {
            uuid: uuid,
            baselineVersion: 1
          }
        })
        .then(res => {
          this.testDataLoading = false;
          if (res.data.code == 0) {
            this.testData.facts = res.data.data;
            this.$refs.modelTable.setCurrentRow(this.testData.facts[0]);
            this.clickTestFact({ index: 0 });
          } else {
            this.$message.error(res.data.msg ? res.data.msg : "加载失败");
          }
        })
        .catch(() => {
          this.$message.error("加载失败");
          this.testDataLoading = false;
        });
    },
    close() {
      this.show = false;
    },
    submitAddAssets(data, dirnaem) {
      this.showAssetsAddDialog = false;
      this.fileName = `${dirnaem}-${data.name}-V${data.select}`;
      this.ruleUuid = data.uuid;
      this.assetsVersion = data.select;
      this.temptest();
    },
    submitTest() {
      this.testData.facts.forEach(fact => {
        fact.fields.forEach(field => {
          if (
            this.$utils.isCollection(field.type) &&
            Array.isArray(field.original)
          ) {
            field.original = JSON.stringify(field.original);
          }
        });
      });
      this.testLoading = true;
      this.testData.result = null;
      this.lastSubmitFacts = this.testData.facts;
      let pkg = {}
      if(JSON.stringify(this.pkg) == "{}" && this.testData.fileData){
        pkg = this.testData.fileData.pkg ? this.testData.fileData.pkg : {}
      }else {
        pkg = this.pkg
      }
      if (!pkg.uuid) {
        this.$message.warning('请选择资源文件')
        this.testLoading = false
        return
      }
      this.$axios
        .post(
          "/knowledgepackage/test",
          this.$qs.stringify({
            uuid:pkg.uuid,
            facts: JSON.stringify(this.testData.facts),
            baselineVersion: 1
          })
        ).then(res => {
          this.testLoading = false;
          if (res.data.code == 0) {
            const data = res.data.data;
            if (typeof data.result.fireNum === "undefined") {
              data.result.fireNum = 0;
            }
            this.testData.result = data.result;
            this.testData.facts = data.facts;
            this.testResult = data.result;
          } else {
            this.$message.error(res.data.msg ? res.data.msg : "测试失败");
          }
        }).catch(() => {
          this.$message.error("测试失败");
          this.testLoading = false;
        });
    },
    saveSubmitTest() {
      if (!this.ruleUuid) {
        this.$message.warning('请选择资源文件')
        return
      }

      this.testData.fileData = {
        fileName: this.fileName,
        ruleUuid: this.ruleUuid,
        assetsVersion: this.assetsVersion,
        pkg: this.pkg ? this.pkg : this.testData.fileData.pkg,
      };
      this.testData.facts.forEach(fact => {
        fact.fields.forEach(field => {
          if (field.value) {
            field.value = "";
          }
        });
      });
      const data = {
        content: JSON.stringify(this.testData),
        ruleUuid: this.ruleUuid,
        uuid: this.assets.uuid,
        assetsVersion: this.assetsVersion
      };
      this.saveLoading = true
      this.$axios
        .post("/testcase/save", this.$qs.stringify(data))
        .then(res => {
          this.saveLoading = false
          if (res.data.code == 0) {
            this.$message.success("保存成功");
          } else {
            this.$message.error(res.data.msg ? res.data.msg : "保存失败");
          }
        }).catch(() => {
          this.$message.error("保存失败");
          this.saveLoading = false
        })
    },
    refresh(){},
    deleData() {
      this.$axios
        .post(
          "/testcase/delete",
          this.$qs.stringify({
            caseUuid: this.assets.uuid
          })
        )
        .then(res => {
          if (res.data.code == 0) {
            this.$message.success("删除成功")
            this.$emit("delete", this.assets.uuid);
          } else {
            this.$message.error(res.data.msg ? res.data.msg : "删除失败")
          }
        })
        .catch(() => {
          this.$message.error("删除失败")
        })
        .then(() => {
          this.loading = false;
        });
    },
    setRowIndex({ row, rowIndex }) {
      row.index = rowIndex;
    },
    clickTestFact(row) {
      this.testData.activeIndex = row.index;
    },
    changeNumberInput(row) {
      if (row.value == 0) {
        row.value = undefined;
      }
    },
    getSubTypeName(subType) {
      if (this.$utils.isObject(subType)) {
        const fact = this.$store.getters.getFactById(subType);
        return fact ? fact.name : "";
      } else {
        return subType;
      }
    },
    getObjectTypeName(type) {
      const fact = this.$store.getters.getFactById(type);
      return fact ? fact.name : "";
    },
    cellClick(row, column, cell, event) {
      const el = this.$refs["input_original_" + row.id][0];
      const ex = this.$refs["input_expectedValue_" + row.id][0];
      if (column.label == "值" && el) {
        this.$nextTick(function() {
          if (this.$utils.isBoolean(row.type)) {
            el.toggleMenu();
          } else {
            el.focus();
          }
        });
      } else if (column.label == "期望值" && ex) {
        this.$nextTick(function() {
          ex.focus();
        });
      }
    },
    chooseHistory(data) {
      if (this.testData.facts.length > 0) {
        let values = {};
        data.forEach(e => {
          e.fields.forEach(f => {
            values[e.id + "_" + f.id] = f.original;
          });
        });
        this.testData.facts.forEach(e => {
          e.fields.forEach(f => {
            f.original = values[e.id + "_" + f.id];
          });
        });
      }
    },
    showFlow(index) {
      const fireFlow = this.testResult.fireNodes[index]
      this.ruleflowTrace.uuid = fireFlow.flowUuid
      this.ruleflowTrace.version = fireFlow.versionNo
      this.ruleflowTrace.nodes = fireFlow.nodes
      this.ruleflowTrace.show = true
    },
    showAssets(index) {
      const fireRule = this.testResult.fireRules[index]
      this.viewingAssets.uuid = fireRule.uuid
      this.viewingAssets.version = fireRule.version
      this.viewingAssets.show = true
    },
    inputInteger(row) {
      return ['Integer', 'Long'].indexOf(row.type) != -1
    }
  }
};
</script>

<style scoped>
.el-alert--success.is-light .el-alert__description .fireNodes {
  color: tomato;
}
.highLightFlow {
  display: inline;
  color: tomato;
  font-size: 16px;
  margin-right: 6px;
  text-decoration: none;
  cursor: pointer;
}

.tb-edit .el-input,
.tb-edit .el-input-number,
.tb-edit .el-select,
.tb-edit .el-dropdown {
  display: none;
}

.tb-edit .current-row .el-input,
.tb-edit .current-row .el-input-number,
.tb-edit .current-row .el-select,
.tb-edit .current-row .el-select + .el-dropdown {
  display: inline-block;
}

.tb-edit .current-row .el-input + span,
.tb-edit .current-row .el-input-number + span,
.tb-edit .current-row .el-select + span,
.tb-edit .current-row .el-dropdown + span {
  display: none;
}
.addFile {
  font-size: 16px;
  margin-bottom: 6px;
}
.addFile .title {
  color: #909399;
}
.addFile .selectAsset {
  cursor: pointer;
  color: #409eff;
}
.testcase .activeClass {
  color: #f56c6c;
}
</style>
