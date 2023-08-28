<template>
  <div>
    <el-card>
      <div class="toolbar">
        <el-button
          type="primary"
          icon="el-icon-plus"
          @click="addModel()"
          v-if="$hasPerm('aimodel:add')">
          添加模型
        </el-button>
        <div class="pull-right">
          <el-input
            placeholder="请输入模型名称"
            v-model.trim="searchName"
            class="search"
            @keyup.enter.native="search">
            <i slot="suffix" class="el-input__icon el-icon-search" @click="search()"></i>
          </el-input>
        </div>
      </div>

      <el-table
        v-loading="tableLoading"
        element-loading-text="数据加载中..."
        :row-class-name="tableRowClassName"
        :data="models"
        @sort-change="sortChange">
        <el-table-column label="模型名称">
          <template slot-scope="scope">
            <span >{{scope.row.modelName}}</span>
          </template>
        </el-table-column>
        <el-table-column prop="jarName" label="模型文件" :show-overflow-tooltip="true"></el-table-column>
        <el-table-column sortable="custom" prop="updateTime" label="更新时间" width="150"></el-table-column>
        <el-table-column sortable="custom" prop="createTime" label="创建时间" width="150"></el-table-column>
        <el-table-column label="操作" width="150">
          <template slot-scope="scope">
            <el-button type="text" @click="edit(scope.row)" v-if="$hasPerm('aimodel:edit')">编辑</el-button>
            <dropdown @command="moreOperation" class="moreOperation" trigger="click">
              <span class="el-dropdown-link">
                更多
                <i class="el-icon-arrow-down el-icon--right"></i>
              </span>
              <dropdown-menu slot="dropdown">
                <dropdown-item
                  :command="{row: scope.row, op: 'download'}"
                  v-if="$hasPerm('aimodel:download')"
                  icon="el-icon-download"
                  >下载模型</dropdown-item>
                <dropdown-item
                  :command="{row: scope.row, op: 'downloadParam'}"
                  v-if="$hasPerm('aimodel:download')"
                  icon="el-icon-download"
                  >下载模型参数</dropdown-item>
                <dropdown-item
                  :command="{row: scope.row, op: 'del'}"
                  v-if="$hasPerm('aimodel:del')"
                  icon="el-icon-delete"
                  >删除</dropdown-item>
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
        @current-change="toPage">
      </el-pagination>
    </el-card>

    <el-dialog
      :title="form.uuid ? '编辑模型' : '添加模型'"
      width="30%"
      :visible.sync="showModelDialog"
      :close-on-click-modal="false"
      @opened="$refs.input.focus()"
      @closed="resetForm">
      <el-form :model="form" :rules="rules" ref="form" label-width="80px" @submit.native.prevent>
        <el-form-item label="模型名称" prop="modelName">
          <el-input
            v-model.trim="form.modelName"
            ref="input"
            :maxlength="100"
            :show-word-limit="false"
            @keyup.enter.native="add">
          </el-input>
        </el-form-item>
        <el-form-item label="模型文件" ref="uploadElement" prop="jarName" >
          <el-upload
            action
            v-model="form.jarName"
            accept=".pmml"
            ref="upload1"
            :limit="1"
            :on-change="changeFile"
            :on-remove="deleteFile"
            :auto-upload="false"
            :show-file-list="true">
            <el-button type="primary" icon="el-icon-files">选择文件</el-button>
            <div slot="tip" class="el-upload__tip">文件不超过30Mb</div>
          </el-upload>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="showModelDialog = false" icon="el-icon-error">取 消</el-button>
        <el-button
          type="primary"
          @click="add"
          icon="el-icon-success"
          :loading="addLoading"
        >{{addLoading ? '提交中...' : '确 定'}}</el-button>
      </div>
    </el-dialog>

    <el-dialog title="引用服务列表" :visible.sync="showMicroDialog">
      <el-table
        v-loading="tableLoading"
        element-loading-text="数据加载中..."
        :data="microData">
        <el-table-column prop="name" label="服务名称"></el-table-column>
        <el-table-column prop="typeName" label="服务类型"></el-table-column>
        <el-table-column label="是否可用" :formatter="row => {return row.enable ? '是' : '否'}"></el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog title="AI模型权限配置" :visible.sync="showPermissionDialog">
      <div class="toolbar">
        <el-cascader
          :options="accounts"
          placeholder="选择要授权的用户"
          @change="handleChange"
          :props="{
                  value:'uuid',
                  label:'username'
              }"
        ></el-cascader>
      </div>
      <div v-for="(item , k) in resourceData" :key="k" class="clearfix" style="line-height:30px;">
        <div style="float:left;">{{item.modelName}}</div>
        <div style="float:right;">
          <span>{{item.permission[0] ? '可见' : '不可见'}}</span>
          <el-switch
            v-model="item.permission[0]"
            active-color="#13ce66"
            @change="setRowClick(item,7)"
          ></el-switch>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
  export default {
    name: "aimodel",
    data() {
      const self = this;
      return {
        showModelDialog: false,
        showPermissionDialog: false,
        tableLoading: false,
        models: [],
        total: 0,
        page: 1,
        searchName: "",
        addLoading: false,
        innerVisible: false,
        form: {
          uuid: "",
          modelName: "",
          jarName: "",
          params: ""
        },
        rules: {
          modelName: [
            { required: true, message: "请输入模型名称", trigger: "blur" },
            { max: 100, message: "长度不超过100个字符", trigger: "blur" }
          ],
          jarName: [
            {
              validator: function(rule, value, callback) {
                if (self.form.uuid) {
                  callback();
                } else if (!self.file) {
                  callback(new Error("请上传模型文件"));
                } else {
                  callback();
                }
              }
            },
            { required: true, message: "请选择模型文件", trigger: "blur" }
            
          ],
          paramName: [
            {
              validator: function(rule, value, callback) {
                if (self.form.uuid) {
                  callback();
                } else {
                  callback();
                }
              }
            }
          ]
        },
        file: null,
        paramFile: null,
        microLoading: false,
        showMicroDialog: false,
        microData: [],
        accounts: [],
        resourceData: [],
        accountUuid: "",
        searchName: "",
        sortOption: {
          sortField: "",
          sortDirection: ""
        }
      };
    },
    mounted() {
      this.initData();
    },
    methods: {
      initData() {
        this.tableLoading = true;
        this.$axios
          .get("/aimodel/all", {
            params: {
              page: this.page,
              isAll: false,
              modelName: this.searchName,
              sortField: this.sortOption.sortField,
              sortDirection: this.sortOption.sortDirection
            }
          })
          .then(res => {
            if (res.data.code == 0) {
              res.data.data.forEach(e => {
                if (e.createTime) {
                  e.createTime = this.$moment(e.createTime).format(
                    "YYYY-MM-DD HH:mm"
                  );
                }
                if (e.updateTime) {
                  e.updateTime = this.$moment(e.updateTime).format(
                    "YYYY-MM-DD HH:mm"
                  );
                }
                e.permission = [false, false, false, false];
              });
              this.total = res.data.total;
              this.models = res.data.data;
              this.resourceData = res.data.data;
            }
            this.tableLoading = false;
          })
          .catch(() => {
            this.tableLoading = false;
          });
      },
      //搜索
      search() {
        this.page = 1;
        this.initData();
      },
      //列表排序
      sortChange(data) {
        this.sortOption.sortField = data.prop;
        this.sortOption.sortDirection =
          data.order == "ascending" ? "asc" : "desc";
        this.initData();
      },
      addModel() {
        //修复点击编辑之后再点击增加模型，状态还是编辑bug
        this.form.uuid = "";
        this.form.modelName = "";
        this.form.jarName = "";
        this.form.params = "";
        this.showModelDialog = true;
      },
      tableRowClassName({ row, rowIndex }) {
        return row.enable === false ? "disabled" : "";
      },
      toPage(page) {
        this.page = page;
        this.initData();
      },
      moreOperation({ row, op }) {
        if (op == "edit") {
          this.edit(row);
        } else if (op == "enable") {
          this.enable(row);
        } else if (op == "del") {
          this.del(row);
        } else if (op == "download") {
          this.download(row);
        } else if (op == "downloadParam") {
          this.downloadParam(row);
        }
      },
      add() {
        console.log(this.rules.jarName);
        this.rules.jarName.pop();
        console.log(this.rules.jarName);
        this.$refs.form.validate(valid => {
          if (valid) {
            this.addLoading = true;
            let url = this.form.uuid ? "/aimodel/edit" : "/aimodel/add";
            let params = new FormData();
            if (this.form.uuid) {
              params.append("uuid", this.form.uuid);
            }
            if (this.file) {
              params.append("formFile", this.file.raw);
            }
            params.append("modelName", this.form.modelName);
            this.$axios.post(url, params, {
              headers: { "Content-Type": "multipart/form-data" }
            }).then(res => {
              this.addLoading = false;
              this.showModelDialog = false;
              if (res.data.code == 0) {
                this.$message.success("操作成功");
                this.page = 1;
                this.initData();
              } else {
                this.$message.error(res.data.msg ? res.data.msg : "操作失败");
                if (this.rules.jarName.length == 1) {
              this.rules.jarName.push({ required: true, message: "请选择模型文件", trigger: "blur" });
            }
              }
            })
            .catch(() => {
              this.$message.error("操作失败");
              this.addLoading = false;
              this.showModelDialog = false;
              if (this.rules.jarName.length == 1) {
              this.rules.jarName.push({ required: true, message: "请选择模型文件", trigger: "blur" });
            }
            });
          } else {
            if (this.rules.jarName.length == 1) {
              this.rules.jarName.push({ required: true, message: "请选择模型文件", trigger: "blur" });
            }
          }
        });
      },
      changeFile(file) {
        this.file = file;
        this.$refs.form.clearValidate('jarName');
      },
      cancelTip() {
        // this.$refs.uploadElement.clearValidate()

      },
      deleteFile() {
        this.form.jarName = null;
        this.file = null;
        if (this.rules.jarName.length == 1) {
              this.rules.jarName.push({ required: true, message: "请选择模型文件", trigger: "blur" });
            }
      },
      resetForm() {
        if (this.rules.jarName.length == 1) {
              this.rules.jarName.push({ required: true, message: "请选择模型文件", trigger: "blur" });
            }
        this.$refs.form.resetFields();
        if (this.file) {
          this.$refs.upload1.clearFiles();
        }
        this.file = null;
      },
      edit(row) {
        this.form.uuid = row.uuid;
        this.form.modelName = row.modelName;
        this.form.params = row.params;
        this.showModelDialog = true;
      },
      enable(row) {
        this.$confirm(`确认${row.enable ? "禁用" : "启用"}`, "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning"
        }).then(() => {
          let url = row.enable ? "/aimodel/disable" : "/aimodel/enable";
          this.tableLoading = true;
          this.$axios.post(url, this.$qs.stringify({
            uuid: row.uuid
          })).then(res => {
            this.tableLoading = false;
            if (res.data.code == 0) {
              this.$message.success("操作成功");
              this.initData();
            } else {
              this.$message.error(res.data.msg ? res.data.msg : "操作失败");
            }
          })
          .catch(() => {
            this.$message.error("操作失败");
            this.tableLoading = false;
          });
        });
      },
      download(row) {
        this.tableLoading = true;
        const that = this;
        this.$axios({
          method: "get",
          url: "/aimodel/down?uuid=" + row.uuid,
          responseType: "blob"
        }).then(res => {
          if (res.data.type == "application/json") {
            var reader = new FileReader();
            reader.onload = function() {
              const json = JSON.parse(reader.result);
              if (json.code == 401) {
                localStorage.removeItem('hasLogin')
                let from = that.$router.currentRoute.fullPath
                that.$router.push('/login?from=' + from)
              } else {
                that.$message.error(json.msg ? json.msg : "下载失败");
              }
            };
            reader.readAsText(res.data);
          } else {
            let url = window.URL.createObjectURL(new Blob([res.data]));
            let a = document.createElement("a");
            a.style.display = "none";
            a.href = url;
            a.target = "_blank";
            a.setAttribute("download", row.jarName);
            document.body.appendChild(a);
            a.click();
          }
          that.tableLoading = false;
        }).catch(() => {
          that.$message.error("下载失败");
          that.tableLoading = false;
        });
      },
      downloadParam(row) {
        this.tableLoading = true;
        const that = this;
        this.$axios({
          method: "get",
          url: "/aimodel/downmodelparams?uuid=" + row.uuid,
          responseType: "blob"
        }).then(res => {
            if (res.data.type == "application/json") {
              var reader = new FileReader();
              reader.onload = function() {
                const json = JSON.parse(reader.result);
                that.$message.error(json.msg ? json.msg : "下载失败");
              };
              reader.readAsText(res.data);
            } else {
              let url = window.URL.createObjectURL(new Blob([res.data]));
              let a = document.createElement("a");
              a.style.display = "none";
              a.href = url;
              a.target = "_blank";
              a.setAttribute("download", row.className + ".xlsx");
              document.body.appendChild(a);
              a.click();
            }
            this.tableLoading = false;
          }).catch(() => {
            that.$message.error("下载失败");
            this.tableLoading = false;
          });
      },
      del(row) {
        this.tableLoading = true;
        this.$confirm(`确认删除?`, "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        }).then(()=>{
          this.$axios.post("/aimodel/delete", this.$qs.stringify({
            uuid: row.uuid
          })).then(res => {
            this.tableLoading = false;
            if (res.data.code == 0) {
              this.$message.success("删除成功");
              this.page = 1;
              this.initData();
            } else {
              this.$message.error(res.data.msg ? res.data.msg : "删除失败");
            }
          }).catch(() => {
            this.$message.error("删除失败");
            this.tableLoading = false;
          });
        })
      },
      microDoalogInfo(row) {
        this.showMicroDialog = true;
        this.microLoading = true;
        this.$axios
          .get("/aimodel/micros", {
            params: {
              uuid: row.uuid
            }
          })
          .then(res => {
            if (res.data.code == 0) {
              this.microData = res.data.data;
            }
          })
          .catch(() => {
            this.microLoading = false;
          })
          .then(() => {
            this.microLoading = false;
          });
      },
      handleChange(v) {
        for (let k in this.resourceData) {
          this.$set(this.resourceData, k, {
            ...this.resourceData[k],
            permission: [false, false, false, false]
          });
        }
        this.setResource(v[0], this.resourceData, 3);
        this.accountUuid = v[0];
      },
      setResource(uuid, obj, dataType) {
        this.$axios
          .post(
            "/permission/accounthas",
            this.$qs.stringify({
              accountUuid: uuid,
              dataType: dataType
            })
          )
          .then(res => {
            if (res.data.code == 0) {
              let data = res.data.data;
              for (let i = 0; i < obj.length; i++) {
                for (let j = 0; j < data.length; j++) {
                  if (obj[i].uuid == data[j].resourceId) {
                    this.$set(obj, i, {
                      ...obj[i],
                      permission: this.infoPermission(data[j].permission)
                    });
                  }
                }
                if (data.length == 0) {
                  this.$set(obj, i, {
                    ...obj[i],
                    permission: [false, false, false, false]
                  });
                }
              }
            }
          });
      },
      infoPermission(arr) {
        let start = ["r", "w", "d", "x"];
        let newPermission = [false, false, false, false];
        let permission = arr.split(",");
        for (let i = 0; i < start.length; i++) {
          for (let j = 0; j < permission.length; j++) {
            if (start[i] == permission[j]) {
              newPermission[i] = true;
            }
          }
        }
        return newPermission;
      },
      setRowClick(item, type) {
        if (this.accountUuid == "") {
          return this.$message.warning("请选择用户");
        }
        let permission = [];
        let arr = ["r", "w", "d", "x"];
        if (item.permission[1] || item.permission[2] || item.permission[3]) {
          item.permission[0] = true;
        }
        for (let i = 0; i < item.permission.length; i++) {
          if (item.permission[i]) {
            permission.push(arr[i]);
          }
        }
        this.$axios
          .post(
            "/permission/authorization",
            this.$qs.stringify({
              accountUuid: this.accountUuid,
              resourceId: item.uuid,
              type: type,
              permission: permission.join(",")
            })
          )
          .then(res => {
            if (res.data.code == 0) {
              this.$message.success("操作成功");
            } else {
              this.$message.error(res.data.msg ? res.data.msg : "操作失败");
            }
          });
      }
    }
  };
</script>

<style scoped>
  .innerVisible {
    margin-left: 80px;
  }
</style>
