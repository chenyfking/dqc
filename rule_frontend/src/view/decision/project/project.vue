<template>
  <div class="projectContent">
    <el-card>
      <div class="toolbar">
        <!-- <el-button
          type="primary"
          icon="el-icon-plus"
          @click="newAdd"
          v-if="$hasPerm('project:add')">
          新建项目
        </el-button> -->
        <!-- <el-upload
          action
          :on-change="importProject"
          :auto-upload="false"
          :disabled="fileLoading"
          :show-file-list="false"
          accept=".project"
          class="upload">
          <el-button
            icon="el-icon-upload"
            :loading="fileLoading"
            v-if="$hasPerm('project:import')">
            {{fileLoading ? '正在导入...' : '导入项目'}}
          </el-button>
        </el-upload> -->
        <div class="pull-right">
          <el-input
            placeholder="请输入项目名称"
            v-model.trim="projectSearchName"
            class="search"
            @keyup.enter.native="search">
            <i slot="suffix" class="el-input__icon el-icon-search" @click="search"></i>
          </el-input>
        </div>
      </div>

      <el-table
        v-loading="tableLoading"
        element-loading-text="数据加载中..."
        :data="projects"
        @sort-change="sortChange"
        @row-dblclick="(row) => $router.push(`/decision/project/${row.uuid}`)">
        <el-table-column label="名称" width="350">
          <template slot-scope="scope">
            <el-button
              type="text"
              @click.stop="$router.push(`/decision/project/${scope.row.uuid}`)"
            >{{scope.row.name}}</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" :show-overflow-tooltip="true"></el-table-column>
        <el-table-column sortable="custom" prop="updateTime" label="更新时间" width="150"></el-table-column>
        <el-table-column sortable="custom" prop="createTime" label="创建时间" width="150"></el-table-column>
        <!-- <el-table-column prop="creator.realname" label="创建人" width="80" show-overflow-tooltip></el-table-column> -->
        <el-table-column label="操作" width="130">
          <template slot-scope="scope">
            <el-button
              type="text"
              @click.stop="$router.push(`/decision/project/${scope.row.uuid}/knowledgepackage`)">
              知识包
            </el-button>
            <dropdown
              @command="moreOperation"
              class="moreOperation"
              v-if="scope.row.userEdit || $hasPerm('project:export')">
              <span class="el-dropdown-link">
                更多<i class="el-icon-arrow-down el-icon--right"></i>
              </span>
              <dropdown-menu slot="dropdown">
                <dropdown-item
                  v-if="scope.row.userEdit"
                  :command="{row: scope.row, op: 'edit'}"
                  icon="el-icon-edit">
                  编辑
                </dropdown-item>
                <dropdown-item
                  :command="{row: scope.row, op: 'export'}"
                  icon="el-icon-download">
                  导出
                </dropdown-item>
                <!-- <dropdown-item
                  v-if="scope.row.userEdit"
                  :command="{row: scope.row, op: 'del'}"
                  icon="el-icon-delete"
                  divided>
                  删除
                </dropdown-item> -->
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
        @current-change="toPage"
      ></el-pagination>
    </el-card>

    <el-dialog
      :title="form.uuid ? '编辑项目' : '新建项目'"
      width="30%"
      :visible.sync="showDialog"
      @opened="$refs.input.focus()"
      :close-on-click-modal="false"
      @closed="$refs.form.resetFields()"
      class="editor-server">
      <el-form :model="form" :rules="rules" ref="form" label-width="80px" @submit.native.prevent>
        <el-form-item label="项目名称" prop="name">
          <el-input
            v-model.trim="form.name"
            ref="input"
            :maxlength="20"
            :show-word-limit="true"
            @keyup.enter.native="submit"
            placeholder="请填写项目名称"
          ></el-input>
        </el-form-item>
        <el-form-item label="项目描述" prop="description">
          <el-input
            type="textarea"
            v-model="form.description"
            :autosize="{ minRows: 6 }"
            :maxlength="100"
            :show-word-limit="true"
            placeholder="还没有描述-这个项目是做什么的？"
          ></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="showDialog = false" icon="el-icon-error">取 消</el-button>
        <el-button type="primary" @click="submit" icon="el-icon-success" :loading="addLoading">
          {{addLoading ? '提交中...' : '确 定'}}
        </el-button>
      </div>
    </el-dialog>

    <el-dialog
      title="成员配置"
      :top="$dialogTop"
      :visible.sync="showMemberDialog"
      @open="openMemberDialog">
      <div v-loading="memberLoading" element-loading-text="数据加载中...">
        <el-transfer
          v-if="showMemberDialog"
          v-model="memberUuids"
          filter-placeholder="搜索"
          :titles="['未选择用户', '已选择用户']"
          @change="changeMember"
          :data="users">
          <el-input
            slot="left-footer"
            size="small"
            placeholder="搜索"
            v-model="userSearchName"
            @keyup.enter.native="userSearch">
            <i slot="suffix" class="el-input__icon el-icon-search" @click="userSearch"></i>
          </el-input>
          <template slot-scope="{option}">
            <el-tooltip :content="option.org.name" placement="left" v-if="option.org && option.org.name">
              <span>{{option.label}}</span>
            </el-tooltip>
            <span v-else>{{option.label}}</span>
          </template>
        </el-transfer>
        <div class="pageContent">
          <el-pagination
            small
            layout="prev, pager, next"
            :total="totalUser"
            hide-on-single-page
            @current-change="toPageUser"
            :page-size="userPageNum"
            :pager-count="3"
            :current-page="userPage">
          </el-pagination>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: "project",
  data() {
    return {
      projects: [],
      tableLoading: false,
      showDialog: false,
      addLoading: false,
      file: null,
      total: 0,
      page: 1,
      form: {
        uuid: "",
        name: "",
        description: "",
      },
      rules: {
        name: [
          { required: true, message: "请输入项目名称", trigger: "blur" },
          { max: 20, message: "长度不超过20个字符", trigger: "blur" },
        ],
        description: [
          { max: 100, message: "长度不超过100个字符", trigger: "blur" },
        ]
      },
      fileLoading: false,
      currentProject: {},
      sortOption: {
        sortField: "",
        sortDirection: "",
      },
      totalUser: 0,
      userPage: 1,
      projectSearchName: "",
      userSearchName: "",
      showMemberDialog: false,
      memberLoading: false,
      members: [],
      users: [],
      memberUuids: [],
      userPageNum: 20
    };
  },
  mounted() {
    this.loadData();
  },
  methods: {
    /**
     * 提交成员
     */
    submitMembers() {
      this.memberLoading = true
      this.$axios.post('/pu/auth', this.$qs.stringify({
        projectUuid: this.currentProject.uuid,
        uuids: this.memberUuids.join(',')
      })).then(res => {
        this.memberLoading = false
        if (res.data.code == 0) {
          this.$message.success('配置成功')
          this.userSearch()
        } else {
          this.$message.error(res.data.msg ? res.data.msg : '配置失败')
        }
      }).catch(() => {
        this.$message.error('配置失败')
        this.memberLoading = false
      })
    },
    sortChange(data) {
      this.sortOption.sortField = data.prop;
      this.sortOption.sortDirection =
        data.order == "ascending" ? "asc" : "desc";
      this.loadData();
    },
    /**
     * 穿梭框变更成员，把变更后的成员对象保存到临时数组里
     */
    changeMember(value, method, array) {
      const members = []
      value.forEach(e =>{
        this.users.some(u => {
          if (u.key == e) {
            members.push(u)
            return true
          }
        })
      })
      this.members = members
      this.submitMembers()
    },
    userSearch() {
      this.userPage = 1;
      this.initUserList();
    },
    /**
     * 获取当前项目成员
     */
    getMembers() {
      this.$axios.get(`/pu/users?uuid=${this.currentProject.uuid}`).then(res => {
        if (res.data.code == 0) {
          if (res.data.data.length > 0) {
            res.data.data.forEach((item, index) => {
              this.memberUuids.push(item.uuid)
              this.members.push({
                key: item.uuid,
                label: item.realname,
                org: item.org
              })
            })
          }
          this.initUserList()
        } else {
          this.$message.error(res.data.msg ? res.data.msg : '获取项目成员失败')
          this.memberLoading = false
          this.showMemberDialog = false
        }
      }).catch(() => {
        this.$message.error('获取项目成员失败')
        this.memberLoading = false
        this.showMemberDialog = false
      })
    },
    initUserList() {
      this.memberLoading = true
      this.$axios.get("/user/searchnotprojectmember", {
        params: {
          projectUuid: this.currentProject.uuid,
          page: this.userPage,
          realname: this.userSearchName,
          pageNum: this.userPageNum
        }
      }).then(res => {
        this.memberLoading = false
        if (res.data.code == 0) {
          this.totalUser = res.data.total;
          let users = []
          res.data.data.forEach((item, index) => {
            users.push({
              key: item.uuid,
              label: item.realname,
              org: item.org
            });
          });
          users = users.concat(this.members)
          this.users = users
        } else {
          this.$message.error(res.data.msg ? res.data.msg : '获取项目成员失败')
        }
      }).catch(() => {
        this.$message.error('获取项目成员失败')
        this.memberLoading = true
      });
    },
    toPageUser(page){
      this.userPage = page
      this.initUserList()
    },
    moreOperation({ row, op }) {
      if (op == "edit") {
        this.edit(row);
      } else if (op == "export") {
        this.exportProject(row);
      } else if (op == "member") {
        this.setMember(row);
      } else if (op == "del") {
        this.del(row);
      }
    },
    setMember(data) {
      this.currentProject = data
      this.showMemberDialog = true
    },
    search() {
      this.page = 1
      this.loadData()
    },
    setRowClick(item, type) {
      if (this.permissions.userUuid == "") {
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
            accountUuid: this.permissions.userUuid,
            resourceId: item.uuid,
            type: type,
            permission: permission.join(","),
          })
        )
        .then((res) => {
          if (res.data.code == 0) {
            this.$message.success("操作成功");
          } else {
            this.$message.error(res.data.msg ? res.data.msg : "操作失败");
          }
        });
    },
    search() {
      this.loadData();
    },
    loadData() {
      this.tableLoading = true;
      this.$axios.get("/project", {
        params: {
          page: this.page,
          name: this.projectSearchName,
          sortField: this.sortOption.sortField,
          sortDirection: this.sortOption.sortDirection,
        }
      }).then(res => {
        if (res.data.code == 0) {
          res.data.data.forEach((e) => {
            if (e.createTime) {
              e.createTime = this.$moment(e.createTime).format("YYYY-MM-DD HH:mm");
            }
            if (e.updateTime) {
              e.updateTime = this.$moment(e.updateTime).format("YYYY-MM-DD HH:mm");
            }
            if (e.creator && !e.creator.realname) {
              e.creator.realname = e.creator.username
            }
          });
          this.projects = res.data.data;
          this.total = res.data.total;
        }
        this.tableLoading = false;
      }).catch(() => {
        this.tableLoading = false;
      });
    },
    submit() {
      this.$refs["form"].validate((valid) => {
        if (valid) {
          this.addLoading = true;
          let url = this.form.uuid ? '/project/edit' : '/project/add'
          let params = {};
          if (this.form.uuid) {
            params.uuid = this.form.uuid;
          }
          params.name = this.form.name;
          params.description = this.form.description;
          this.$axios
            .post(url, this.$qs.stringify(params))
            .then((res) => {
              this.addLoading = false;
              this.showDialog = false;
              if (res.data.code == 0) {
                if (!this.form.uuid) {
                  this.$router.push("/decision/project/" + res.data.data.uuid);
                } else {
                  this.$message.success("编辑成功");
                  this.loadData();
                }
              } else {
                this.$message.error(res.data.msg ? res.data.msg : "操作失败");
              }
            })
            .catch(() => {
              this.$message.error("操作失败");
              this.addLoading = false;
              this.showDialog = false;
            });
        }
      });
    },
    toPage(p) {
      this.page = p;
      this.loadData();
    },
    edit(row) {
      this.form.uuid = row.uuid;
      this.form.name = row.name;
      this.form.description = row.description;
      this.showDialog = true;
    },
    newAdd() {
      this.form = {
        uuid: "",
        name: "",
        description: ""
      };
      this.showDialog = true;
    },
    exportProject(row) {
      const that = this;
      that.tableLoading = true;
      this.$axios({
        method: "get",
        url: "/project/export?uuid=" + row.uuid,
        responseType: "blob",
      }).then(res => {
        if (res.data.type == "application/json") {
          const reader = new FileReader();
          reader.onload = function () {
            const json = JSON.parse(reader.result);
            if (json.code == 401) {
              localStorage.removeItem('hasLogin')
              const from = that.$router.currentRoute.fullPath
              that.$router.push('/login?from=' + from)
            } else {
              that.$message.error(json.msg ? json.msg : "导出失败");
            }
          };
          reader.readAsText(res.data);
        } else {
          let url = window.URL.createObjectURL(new Blob([res.data]));
          let a = document.createElement("a");
          a.style.display = "none";
          a.href = url;
          a.target = "_blank";
          a.setAttribute("download", row.name + ".project");
          document.body.appendChild(a);
          a.click();
        }
        that.tableLoading = false;
      }).catch(() => {
        that.$message.error("导出失败");
        that.tableLoading = false;
      });
    },
    del(row) {
      this.$confirm(`确认删除?`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      }).then(() => {
        this.tableLoading = true;
        this.$axios
          .post(
            "/project/delete",
            this.$qs.stringify({
              uuid: row.uuid,
            })
          )
          .then((res) => {
            this.tableLoading = false;
            if (res.data.code == 0) {
              this.$message.success("删除成功");
              this.loadData();
            } else {
              this.$message.error(res.data.msg ? res.data.msg : "删除失败");
            }
          })
          .catch(() => {
            this.$message.error("删除失败");
            this.tableLoading = false;
          });
      });
    },
    importProject(e) {
      if (!this.$utils.checkUploadFile(e, 'project')) return

      let config = {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      };
      let form = new FormData();
      form.append("file", e.raw);
      this.fileLoading = true;
      this.$axios
        .post("/project/import", form, config)
        .then((res) => {
          if (res.data.code == 0) {
            this.$message.success("导入成功");
            this.$router.push('/decision/project/' + res.data.data.uuid)
          } else {
            this.$message.error(res.data.msg ? res.data.msg : "导入失败");
          }
          this.fileLoading = false;
        })
        .catch(() => {
          this.$message.error("导入失败");
          this.fileLoading = false;
        });
    },
    openMemberDialog() {
      this.memberLoading = true
      this.members = []
      this.users = []
      this.memberUuids = []
      this.getMembers()
    }
  },
};
</script>

<style scoped>
  .pageContent {
    width: 200px;
  }
  .el-transfer {
    display: flex;
    align-items: center;
    justify-content: space-around;
  }
</style>
