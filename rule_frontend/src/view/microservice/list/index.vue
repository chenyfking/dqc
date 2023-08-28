<template>
  <div>
    <el-card>
      <div class="toolbar">
        <el-button
          type="primary"
          v-if="$hasPerm('microservice:trace')"
          @click="showTraceDialog = true"
          icon="el-icon-search">
          查询轨迹
        </el-button>
        <div class="pull-right">
          <el-input
            placeholder="搜索"
            v-model.trim="searchName"
            class="search"
            @keyup.enter.native="search">
            <i slot="suffix" class="el-input__icon el-icon-search" @click="search"></i>
          </el-input>
        </div>
      </div>

      <el-table
        v-loading="tableLoading"
        element-loading-text="数据加载中..."
        :data="tableData">
        <el-table-column label="名称" prop="name" show-overflow-tooltip></el-table-column>
        <el-table-column label="所属项目" prop="projectName" show-overflow-tooltip></el-table-column>
        <el-table-column label="所属用户" prop="projectCreatorName" width="80" show-overflow-tooltip></el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="150"></el-table-column>
        <el-table-column label="操作">
          <template slot-scope="scope">
            <el-button v-if="$hasPerm('microservice:online')" type="text" @click="deploy(scope.row)">生效上线</el-button>
            <el-button v-if="$hasPerm('microservice:edit')" type="text" @click="setApprovalLabel(scope.row)">设置审批字段</el-button>
            <dropdown @command="moreOperation" class="moreOperation">
              <span class="el-dropdown-link">
                更多<i class="el-icon-arrow-down el-icon--right"></i>
              </span>
              <dropdown-menu slot="dropdown">
                <dropdown-item
                  v-if="$hasPerm('microservice:apidoc')"
                  :command="{row: scope.row, op: 'apidoc'}"
                  icon="el-icon-document">
                  调用文档
                </dropdown-item>
                <dropdown-item
                  :command="{row: scope.row, op: 'edit'}"
                  icon="el-icon-edit"
                  v-if="$hasPerm('microservice:edit')">
                  编辑服务
                </dropdown-item>
                <dropdown-item
                  v-if="$hasPerm('microservice:client')"
                  :command="{row: scope.row, op: 'view'}"
                  icon="el-icon-monitor">
                  上线节点
                </dropdown-item>
                <dropdown-item
                  :command="{row: scope.row, op: 'del'}"
                  icon="el-icon-circle-close"
                  v-if="$hasPerm('microservice:shutdown')">
                  停用服务
                </dropdown-item>
              </dropdown-menu>
            </dropdown>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        background
        layout="total, prev, pager, next, jumper"
        :total="total"
        hide-on-single-page
        :current-page.sync="page"
        @current-change="initData">
      </el-pagination>
    </el-card>

    <el-dialog
      title="编辑服务"
      :visible.sync="showEditDialog"
      width="30%"
      @opened="$refs.nameInput.focus()"
      :close-on-click-modal="false">
      <el-form v-model="form" ref="form" label-width="100px" @submit.native.prevent>
        <el-form-item label="服务名称">
          <el-input
            ref="nameInput"
            type="text"
            v-model.trim="form.name"
            :maxlength="20"
            show-word-limit
            @keyup.enter.native="submitEdit"
            placeholder="请填写服务名称">
          </el-input>
        </el-form-item>
        <el-form-item label="服务简介">
          <el-input
            type="textarea"
            :value="form.description"
            :autosize="{ minRows: 6 }"
            :maxlength="100"
            show-word-limit
            v-model="form.description"
            placeholder="请填写服务简介">
          </el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="showEditDialog = false" icon="el-icon-error">取 消</el-button>
        <el-button
          type="primary"
          @click="submitEdit"
          icon="el-icon-success"
          :loading="editLoading">
          {{editLoading ? '提交中...' : '确 定'}}
        </el-button>
      </div>
    </el-dialog>

    <el-dialog title="引用AI模型列表" :visible.sync="showAiModelDialog">
      <el-table v-loading="aiModelLoading" element-loading-text="数据加载中..." :data="aiModelData">
        <el-table-column prop="modelName" label="模型名称"></el-table-column>
        <el-table-column label="是否可用" :formatter="row => {return row.enable ? '是' : '否'}"></el-table-column>
        <el-table-column prop="createTime" label="创建时间"></el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog title="服务上线节点" :visible.sync="showAiClientDialog">
      <el-table v-loading="showAiClientLoading" element-loading-text="数据加载中..." :data="aiClientData">
        <el-table-column prop="name" label="名称"></el-table-column>
        <el-table-column prop="baseUrl" label="地址"></el-table-column>
        <el-table-column label="是否启用" align="center">
          <template slot-scope="scope">
            <i
              class="el-icon-success"
              style="color: green; font-size: 18px;"
              v-if="!scope.row.disabled">
            </i>
            <i class="el-icon-error" style="color: red; font-size: 18px;" v-else></i>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog
      title="查询轨迹"
      :visible.sync="showTraceDialog"
      :top="$dialogTop"
      @opened="$refs.seqNoInput.focus()"
      @closed="closeTraceDialog">
      <el-form ref="traceForm" :model="traceForm" label-width="80px" inline @submit.native.prevent>
        <el-form-item label="流水号" prop="seqNo">
          <el-input
            ref="seqNoInput"
            v-model="traceForm.seqNo"
            @keyup.enter.native="submitQueryTrace"
            placeholder="搜索">
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            @click="submitQueryTrace"
            icon="el-icon-success"
            :loading="traceLoading">
            {{traceLoading ? '提交中...' : '搜 索'}}
          </el-button>
          <el-button
            @click="showTraceDialog = false"
            icon="el-icon-error">
            取 消
          </el-button>
        </el-form-item>
      </el-form>
      <div v-if="traceResult != null" style="padding: 0px 20px;">
        <p>触发了<b>{{traceResult.fireNum}}</b>条规则，耗时<b>{{traceResult.fireTime}}</b>毫秒</p>
        <template v-if="traceResult.fireNodes && traceResult.fireNodes.length > 0">
          <p>决策流轨迹：</p>
          <p v-for="(e, i) in traceResult.fireNodes" :key="e.processId" style="margin-left: 20px">
            <el-link
              type="info"
              @click="showFlow(i)"
              icon="el-icon-view">
              {{e.flowName}}
            </el-link>
          </p>
        </template>
        <template v-if="traceResult.fireNum > 0">
          <p>规则列表：</p>
          <p v-for="(e, i) in traceResult.fireRules" :key="e.name" style="margin-left: 20px">
            <el-link
              type="info"
              @click="showAssets(i)"
              icon="el-icon-view">
              {{e.name}}
            </el-link>
          </p>
        </template>
        <p>执行结果：</p>
        <el-row :gutter="10">
          <el-col :span="6">
            <el-table
              :data="resData.facts"
              :row-style="{cursor: 'pointer'}"
              :row-class-name="setRowIndex"
              @row-click="clickResFact"
              highlight-current-row
              :current-row-key="resData.facts[resData.activeIndex].id"
              row-key="id"
              border
              ref="modelTable">
              <el-table-column prop="name" label="数据结构" align="center"></el-table-column>
            </el-table>
          </el-col>
          <el-col :span="18">
            <el-table
              v-for="(fact, index) in resData.facts"
              :key="fact.id"
              v-show="index == resData.activeIndex"
              :data="fact.fields"
              :row-style="{cursor: 'pointer'}"
              highlight-current-row
              border>
              <el-table-column prop="label" label="中文名称" align="center"></el-table-column>
              <el-table-column label="结果" align="center">
                <template slot-scope="scope">
                  <el-popover
                    v-if="$utils.isObject(scope.row.type) || $utils.isCollection(scope.row.type)"
                    placement="right"
                    width="400"
                    trigger="click">
                    <span>{{scope.row.value}}</span>
                    <el-link icon="el-icon-view" slot="reference"></el-link>
                  </el-popover>
                  <span v-else>{{scope.row.value}}</span>
                </template>
              </el-table-column>
            </el-table>
          </el-col>
        </el-row>
      </div>
    </el-dialog>

    <approval-label
      v-if="showbaseLineDialog"
      :active.sync="showbaseLineDialog"
      :pkg="rowData"
      @reloadData="reloadData">
    </approval-label>
    <deployment :show.sync="deployment.show" :micro-uuid="deployment.microUuid"></deployment>

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
  </div>
</template>

<script>
import approvalLabel from "./approvalLabel";
import deployment from "./deployment";

export default {
  name: "index",
  components: {
    'approvalLabel': approvalLabel,
    'deployment': deployment
  },
  data() {
    return {
      rowData:'',
      tableLoading: false,
      tableData: [],
      total: 0,
      page: 1,
      showEditDialog: false,
      showDeployDialog: false,
      editLoading: false,
      value: "",
      form: {
        uuid: "",
        name: "",
        type: "",
        description: ""
      },
      types: [],
      showAiModelDialog: false,
      aiModelLoading: false,
      aiModelData: [],
      searchName: "",
      showAiClientDialog: false,
      showAiClientLoading: false,
      aiClientData: [],
      showbaseLineDialog: false,
      showTraceDialog: false,
      traceForm: {
        seqNo: ''
      },
      traceLoading: false,
      traceResult: null,
      traceProjectUuid: null,
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
      },
      resData: {
        activeIndex: 0,
        facts: []
      },
      deployment: {
        microUuid: '',
        show: false
      }
    };
  },
  mounted() {
    this.initData();
  },
  methods: {
    initData() {
      this.tableLoading = true;
      this.$axios.get("/micro", {
        params: {
          page: this.page,
          name: this.searchName
        }
      }).then(res => {
        if (res.data.code == 0) {
          if (res.data.total > 0) {
            res.data.data.forEach(e => {
              if (e.createTime) {
                e.createTime = this.$moment(e.createTime).format("YYYY-MM-DD HH:mm");
              }
              if (e.updateTime) {
                e.updateTime = this.$moment(e.updateTime).format("YYYY-MM-DD HH:mm");
              }
            });
            this.tableData = res.data.data;
          } else {
            this.tableData = [];
          }
          this.total = res.data.total;
        }
        this.tableLoading = false;
      }).catch(() => {
        this.tableLoading = false;
      });
    },
    reloadData() {
      this.initData();
    },
    moreOperation({ row, op }) {
      if (op == "edit") {
        this.edit(row);
      } else if (op == "del") {
        this.del(row);
      } else if (op == "apidoc") {
        this.apidoc(row);
      } else if (op == "online") {
        this.online(row);
      } else if (op == "view") {
        this.viewClient(row);
      }
    },
    del(row) {
      this.$confirm("此操作将停用该服务, 是否继续?", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(() => {
        this.tableLoading = true;
        this.$axios.post("/microdeployment/offline", this.$qs.stringify({
          microUuid: row.uuid,
          packageUuid: row.packageUuid
        })).then(res => {
          this.tableLoading = false;
          if (res.data.code == 0) {
            this.$message.success("停用成功")
            this.initData();
          } else {
            this.$message.error(res.data.msg ? res.data.msg : "停用失败")
          }
        }).catch(() => {
          this.$message.error("停用失败")
          this.tableLoading = false;
        });
      });
    },
    setApprovalLabel(row){
      this.rowData = row
      this.showbaseLineDialog= true
    },
    deploy(row){
      this.deployment.microUuid = row.uuid
      this.deployment.show = true
    },
    edit(row) {
      this.showEditDialog = true;
      this.form.uuid = row.uuid;
      this.form.name = row.name;
      this.form.type = row.typeName;
      this.form.description = row.description;
    },
    submitEdit() {
      this.editLoading = true;
      this.$axios
        .post(
          "/micro/update",
          this.$qs.stringify({
            uuid: this.form.uuid,
            name: this.form.name,
            typeName: this.form.type,
            description: this.form.description
          })
        )
        .then(res => {
          this.editLoading = false;
          if (res.data.code == 0) {
            this.$message.success("编辑成功")
            this.initData();
            this.showEditDialog = false;
          } else {
            this.$message.error(res.data.msg ? res.data.msg : "编辑失败")
          }
        })
        .catch(() => {
          this.$message.error("编辑失败")
          this.editLoading = false;
        });
    },
    apidoc(row) {
      this.$router.push({ path: "/microservice/" + row.uuid + "/apidoc" });
    },
    online(row) {
      this.$axios
        .post(
          "/micro/update",
          this.$qs.stringify({
            uuid: row.uuid,
            onlinApproval: 1
          })
        )
        .then(res => {
          if (res.data.code == 0) {
            this.$message.success("操作成功");
            row.onlinApproval = 1;
          } else {
            this.$message.error(res.data.msg ? res.data.msg : "操作失败");
          }
        });
    },
    viewClient(row) {
      this.showAiClientDialog = true;
      this.showAiClientLoading = true;
      this.$axios
        .get("/client/bymicro/" + row.uuid)
        .then(res => {
          if (res.data.code == 0) {
            this.aiClientData = res.data.data;
          }
          this.showAiClientLoading = false;
        })
        .catch(() => {
          this.showAiClientLoading = false;
        });
    },
    //搜索服务
    search() {
      this.page = 1
      this.initData();
    },
    submitQueryTrace() {
      if (!this.traceForm.seqNo) {
        this.$message.warning('请输入流水号')
        return
      }

      this.traceLoading = true
      this.traceResult = null
      this.$axios.get('/decision/trace?seqNo=' + this.traceForm.seqNo).then(res => {
        this.traceLoading = false
        if (res.data.code == 0) {
          this.traceResult = res.data.data.trace
          if (this.traceProjectUuid != res.data.data.projectUuid) {
            this.traceProjectUuid = res.data.data.projectUuid
            this.$store.dispatch('loadBoms', {
              projectUuid: this.traceProjectUuid,
              callback: () => {
                this.parseResData(res.data.data.result)
              }
            })
          } else {
            this.parseResData(res.data.data.result)
          }
        } else {
          this.$message.error(res.data.msg ? res.data.msg : '查询失败')
        }
      }).catch(e => {
        this.$message.error('查询失败')
        this.traceLoading = false
      })
    },
    showFlow(index) {
      const fireFlow = this.traceResult.fireNodes[index]
      this.ruleflowTrace.uuid = fireFlow.flowUuid
      this.ruleflowTrace.version = fireFlow.versionNo
      this.ruleflowTrace.nodes = fireFlow.nodes
      this.ruleflowTrace.show = true
    },
    showAssets(index) {
      const fireRule = this.traceResult.fireRules[index]
      this.viewingAssets.uuid = fireRule.uuid
      this.viewingAssets.version = fireRule.version
      this.viewingAssets.fireRules = [fireRule.name]
      this.viewingAssets.show = true
    },
    closeTraceDialog() {
      this.traceForm.seqNo = ''
      this.traceResult = null
    },
    setRowIndex({row, rowIndex}) {
      row.index = rowIndex
    },
    clickResFact(row) {
      this.resData.activeIndex = row.index
    },
    parseResData(result) {
      const resJson = JSON.parse(result)
      const facts = []
      Object.keys(resJson).forEach(factKey => {
        this.$store.state.facts.some(fact => {
          if (factKey == fact.enName) {
            const item = {
              id: fact.id,
              name: fact.name,
              fields: []
            }

            Object.keys(resJson[factKey]).forEach(fieldKey => {
              fact.fields.some(field => {
                if (fieldKey == field.name) {
                  let value = resJson[factKey][fieldKey]
                  if (this.$utils.isBoolean(field.type)) {
                    value = (value === 'true' ? '是' : '否')
                  } else if (this.$utils.isObject(field.type) || this.$utils.isCollection(field.type)) {
                    try {
                      value = JSON.stringify(value)
                    } catch (e) {
                      console.error(e)
                    }
                  }
                  item.fields.push({
                    label: field.label,
                    value: value,
                    type: field.type
                  })
                  return true
                }
              })
            })
            facts.push(item)
            return true
          }
        })
      })
      this.resData.facts = facts
      this.clickResFact({index: 0})
    }
  }
};
</script>

<style scoped>
</style>
