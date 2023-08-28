<template>
  <el-dialog
    title="仿真测试"
    :top="$dialogTop"
    :visible.sync="show"
    width="70%"
    append-to-body
    @open="open"
    @closed="$emit('update:active', false)"
    :close-on-click-modal="false">
    <div class="toolbar">
      <dropdown
        split-button
        type="primary"
        @command="repeatSubmitTest"
        :loading="testLoading"
        @click="() => !testLoading && submitTest()">
        <i :class="testLoading ? 'el-icon-loading' : 'el-icon-lightning'"></i>
        {{testLoading ? '提交中...' : '测试'}}
        <dropdown-menu slot="dropdown">
          <dropdown-item icon="el-icon-caret-right" :disabled="lastSubmitFacts == null">重复上次测试</dropdown-item>
        </dropdown-menu>
      </dropdown>

      <dropdown split-button @click="downloadExcel" v-loading="batchLoading">
        <i class="el-icon-download"></i>
        下载批量测试模版
        <dropdown-menu slot="dropdown">
          <dropdown-item>
            <el-upload
              action
              :on-change="uploadExcel"
              :auto-upload="false"
              :show-file-list="false"
              accept=".xlsx">
              <i class="iconfont iconVersionupdaterule-copy"></i> 上传批量测试数据
            </el-upload>
          </dropdown-item>
        </dropdown-menu>
      </dropdown>
    </div>

    <el-alert
      v-show="showTestResult && testData.result"
      class="toolbar"
      title="测试成功"
      :closable="false"
      type="success"
      show-icon>
      <p>触发了<b>{{testResult.fireNum}}</b>条规则，耗时<b>{{testResult.fireTime}}</b>毫秒</p>
      <template v-if="testResult.fireNodes && testResult.fireNodes.length > 0">
        <p>决策流轨迹：</p>
        <p v-for="(e, i) in testResult.fireNodes" :key="e.processId" style="margin-left: 20px">
          <el-link type="success"
                   @click="showFlow(i)"
                   icon="el-icon-view">
            {{e.flowName}}
          </el-link>
        </p>
      </template>
      <template v-if="testResult.fireNum > 0">
        <p>规则列表：</p>
        <p v-for="(e, i) in testResult.fireRules" :key="e.name" style="margin-left: 20px">
          <el-link type="success"
                   @click="showAssets(i)"
                   icon="el-icon-view">
            {{e.name}}
          </el-link>
        </p>
      </template>
    </el-alert>

    <el-alert
      v-if="showBatchResult"
      class="toolbar"
      title="批量测试成功"
      :closable="false"
      type="success"
      show-icon>
      <el-link
        type="success"
        icon="el-icon-download"
        @click="downloadBatchResult">
        下载结果文件
      </el-link>
    </el-alert>

    <el-row v-loading="testDataLoading" :gutter="10" v-if="testData.facts.length > 0">
      <el-col :span="6">
        <el-table
          :data="testData.facts"
          :row-style="{cursor: 'pointer'}"
          :row-class-name="setRowIndex"
          @row-click="clickTestFact"
          highlight-current-row
          :current-row-key="testData.facts[testData.activeIndex].id"
          row-key="id"
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
          :row-style="{cursor: 'pointer'}"
          class="tb-edit"
          highlight-current-row
          border>
          <el-table-column label="值" align="center">
            <template slot-scope="scope">
              <el-input-number
                v-if="$utils.isNumber(scope.row.type)"
                v-model="scope.row.original"
                :ref="'input_original_' + scope.row.id"
                style="width: 100%;"
                :step-strictly="inputInteger(scope.row)"
                :min="inputMin(scope.row)"
                :max="inputMax(scope.row)"
                :controls="false"></el-input-number>
              <el-select
                v-else-if="$utils.isBoolean(scope.row.type)"
                v-model="scope.row.original"
                :ref="'input_original_' + scope.row.id"
                placeholder="请选择">
                <el-option value="true" label="是"></el-option>
                <el-option value="false" label="否"></el-option>
              </el-select>
              <el-date-picker
                v-else-if="$utils.isDate(scope.row.type)"
                v-model="scope.row.original"
                type="datetime"
                :ref="'input_original_' + scope.row.id"
                value-format="yyyy-MM-dd HH:mm:ss"
                placeholder="请选择"
                align="center"
                style="width: 100%;"></el-date-picker>
              <test-collection
                v-else-if="$utils.isCollection(scope.row.type)"
                v-model="scope.row.original"
                :ref="'input_original_' + scope.row.id"
                :type="scope.row.subType"></test-collection>
              <test-object
                v-else-if="$utils.isObject(scope.row.type)"
                v-model="scope.row.original"
                :id="scope.row.subType"
                :ref="'input_original_' + scope.row.id">
              </test-object>
              <el-input
                v-model.trim="scope.row.original"
                v-else
                :ref="'input_original_' + scope.row.id"></el-input>
              <span
                v-if="$utils.isBoolean(scope.row.type)">
                {{!scope.row.original ? '' : (scope.row.original === 'true' ? '是' : '否')}}
              </span>
              <span v-else-if="!$utils.isCollection(scope.row.type) && !$utils.isObject(scope.row.type)">
                {{scope.row.original}}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="结果" align="center">
            <template slot-scope="scope">
              <span v-if="$utils.isBoolean(scope.row.type)">
                {{!scope.row.value ? '' : (scope.row.value === 'true' ? '是' : '否')}}
              </span>
              <el-popover
                v-else-if="($utils.isObject(scope.row.type) || $utils.isCollection(scope.row.type)) && scope.row.value"
                placement="right"
                width="400"
                trigger="click">
                <el-input type="textarea" :rows="10" :value="scope.row.value" readonly></el-input>
                <el-link icon="el-icon-view" slot="reference"></el-link>
              </el-popover>
              <span v-else>{{scope.row.value}}</span>
            </template>
          </el-table-column>
          <el-table-column prop="label" label="中文名称" align="center"></el-table-column>
          <el-table-column label="数据类型" align="center">
            <template slot-scope="scope">
              <span v-if="$utils.isCollection(scope.row.type)">
                {{scope.row.type + '<' + getSubTypeName(scope.row.subType) + '>'}}
              </span>
              <span v-else-if="$utils.isObject(scope.row.type)">
                {{getObjectTypeName(scope.row.subType)}}
              </span>
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
      :fire-rules="viewingAssets.fireRules"
      :show.sync="viewingAssets.show">
    </assets-view>
  </el-dialog>
</template>

<script>
export default {
  name: "test-dialog",
  props: {
    active: Boolean
  },
  watch: {
    active: function(newVal) {
      this.show = newVal;
    }
  },
  computed: {
    pkg() {
      return this.$store.state.pkg;
    }
  },
  data() {
    return {
      show: this.active,
      testLoading: false,
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
      showTestResult: false,
      showBatchResult: false,
      downloadBatchResultLink: null,
      ruleflowTrace: {
        uuid: '',
        version: 0,
        nodes: [],
        show: false
      },
      viewingAssets: {
        uuid: '',
        version: 0,
        show: false,
        fireRules: []
      }
    };
  },
  methods: {
    open() {
      this.testDataLoading = true;
      this.testData.result = null;
      this.showBatchResult = false;
      this.$axios.get("/knowledgepackage/testdata", {
        params: {
          uuid: this.pkg.packageUuid?this.pkg.packageUuid:this.pkg.uuid,
          baselineVersion: this.pkg.baselineVersion,
        }
      }).then(res => {
        this.testDataLoading = false;
        if (res.data.code == 0) {
          this.testData.facts = res.data.data;
          this.testData.facts.forEach(fact => {
            fact.fields = fact.fields.filter(field => !this.$utils.isDerive(field.type))
          })
          this.$refs.modelTable.setCurrentRow(this.testData.facts[0]);
          this.clickTestFact({ index: 0 });
        } else {
          this.$message.error(res.data.msg ? res.data.msg : "加载失败");
        }
      }).catch(() => {
        this.testDataLoading = false;
      });
    },
    close() {
      this.show = false;
    },
    submitTest() {
      this.testData.facts.forEach(fact => {
        fact.fields.forEach(field => {
          if (this.$utils.isCollection(field.type) && Array.isArray(field.original)) {
            field.original = JSON.stringify(field.original)
          }
        })
      })
      this.testLoading = true;
      this.testData.result = null;
      this.lastSubmitFacts = this.testData.facts;
      this.showBatchResult = false;
      this.$axios.post("/knowledgepackage/test", this.$qs.stringify({
        uuid: this.pkg.packageUuid ? this.pkg.packageUuid : this.pkg.uuid,
        baselineVersion: this.pkg.baselineVersion,
        facts: JSON.stringify(this.testData.facts)
      })).then(res => {
        this.testLoading = false;
        if (res.data.code == 0) {
          const data = res.data.data;
          if (typeof data.result.fireNum === "undefined") {
            data.result.fireNum = 0;
          }

          this.testData.result = data.result;
          this.testData.facts = data.facts;
          this.testResult = data.result
          this.showTestResult = true
        } else {
          if (res.data.msg) {
            const msg = res.data.msg.replace(/\n/g, '<br>').replace(/ /g, '&ensp;')
            msg = "规则配置有误,请检测您自定义的数据结构和规则等文件配置"
            this.$message.error({
              message: msg,
              duration: 5000,
              dangerouslyUseHTMLString: true,
              showClose: true
            })
          } else {
            this.$message.error("规则配置有误,测试失败,请检查配置")
          }
        }
      }).catch(() => {
        this.$message.error("测试失败, 请检查您配置的规则及数据结构是否有误");
        this.testLoading = false;
      });
    },
    repeatSubmitTest() {
      if (this.lastSubmitFacts != null) {
        this.testData.facts = this.lastSubmitFacts;
        this.submitTest();
      }
    },
    downloadExcel() {
      const uuid =  this.pkg.packageUuid ? this.pkg.packageUuid : this.pkg.uuid
      const baselineVersion = this.pkg.baselineVersion ? this.pkg.baselineVersion : 0

      const that = this;
      this.$axios({
        method: "get",
        url: "/knowledgepackage/batchmodel?uuid=" + uuid + "&baselineVersion=" + baselineVersion,
        responseType: "blob"
      }).then(res => {
        if (res.data.type == "application/json") {
          var reader = new FileReader();
          reader.onload = function() {
            const json = JSON.parse(reader.result);
            if (json.code == 401) {
              localStorage.removeItem('hasLogin')
              const from = that.$router.currentRoute.fullPath
              that.$router.push('/login?from=' + from)
            } else {
              that.$message.error(json.msg ? json.msg : "下载失败,请检查数据结构");
            }
          };
          reader.readAsText(res.data);
        } else {
          let url = window.URL.createObjectURL(new Blob([res.data]));
          let a = document.createElement("a");
          a.style.display = "none";
          a.href = url;
          a.target = "_blank";
          a.setAttribute("download", '批量测试模板.xlsx');
          document.body.appendChild(a);
          a.click();
        }
      }).catch(() => {
        that.$message.error("下载失败,请检查数据结构");
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
      const fact = this.$store.getters.getFactById(type)
      return fact ? fact.name : ''
    },
    cellClick(row, column, cell, event) {
      const el = this.$refs["input_original_" + row.id][0];
      if (column.label == '值' && el) {
        this.$nextTick(function() {
          if (this.$utils.isBoolean(row.type)) {
            el.toggleMenu();
          } else {
            el.focus();
          }
        })
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
      this.viewingAssets.fireRules = [fireRule.name]
      this.viewingAssets.show = true
    },
    uploadExcel(e) {
      if (!this.$utils.checkUploadFile(e, ['xlsx', 'xls'])) return

      const uuid =  this.pkg.packageUuid ? this.pkg.packageUuid : this.pkg.uuid
      const baselineVersion = this.pkg.baselineVersion ? this.pkg.baselineVersion : 0
      const form = new FormData();
      form.append("file", e.raw);
      form.append("uuid", uuid);
      form.append("baselineVersion", baselineVersion);

      const that = this;
      that.batchLoading = true
      that.showTestResult = false
      that.showBatchResult = false
      this.$axios({
        method: "post",
        url: "/knowledgepackage/batchtest",
        data: form,
        responseType: "blob"
      }).then(res => {
        if (res.data.type == "application/json") {
          var reader = new FileReader();
          reader.onload = function() {
            const json = JSON.parse(reader.result);
            if (json.code == 401) {
              localStorage.removeItem('hasLogin')
              const from = that.$router.currentRoute.fullPath
              that.$router.push('/login?from=' + from)
            } else {
              that.$message.error(json.msg ? json.msg : "批量测试失败");
            }
          };
          reader.readAsText(res.data);
        } else {
          let url = window.URL.createObjectURL(new Blob([res.data]));
          let a = document.createElement("a");
          a.style.display = "none";
          a.href = url;
          a.target = "_blank";
          const dotIndex = e.name.lastIndexOf('.')
          if (dotIndex != -1) {
            a.setAttribute("download", '批量测试结果' + e.name.substring(dotIndex));
          } else {
            a.setAttribute("download", '批量测试结果.xlsx');
          }
          document.body.appendChild(a);
          that.showBatchResult = true
          that.downloadBatchResultLink = a
        }
        that.batchLoading = false
      }).catch(() => {
        that.$message.error("批量测试失败");
        that.batchLoading = false
      });
    },
    downloadBatchResult() {
      this.downloadBatchResultLink.click()
    },
    /**
     * 是否只能输入整数
     */
    inputInteger(row) {
      return ['Integer', 'Long'].indexOf(row.type) != -1
    },
    inputMin(row) {
      if (row.type == 'Integer') {
        return -2147483648
      } else if (row.type == 'Long') {
        return -9223372036854775808
      }
    },
    inputMax(row) {
      if (row.type == 'Integer') {
        return 2147483647
      } else if (row.type == 'Long') {
        return 9223372036854775807
      }
    }
  }
};
</script>

<style scoped>
  .el-alert--success.is-light .el-alert__description .fireNodes {
    color: tomato;
  }

  .tb-edit .el-input,
  .tb-edit .el-input-number,
  .tb-edit .el-select,
  .tb-edit .el-dropdown {
    display: none
  }

  .tb-edit .current-row .el-input,
  .tb-edit .current-row .el-input-number,
  .tb-edit .current-row .el-select,
  .tb-edit .current-row .el-select + .el-dropdown {
    display: inline-block
  }

  .tb-edit .current-row .el-input + span,
  .tb-edit .current-row .el-input-number + span,
  .tb-edit .current-row .el-select + span,
  .tb-edit .current-row .el-dropdown + span {
    display: none
  }
</style>
