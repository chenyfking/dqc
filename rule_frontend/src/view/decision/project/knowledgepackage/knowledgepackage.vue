<template>
  <div>
    <div class="toolbar">
      <el-button
        type="primary"
        icon="el-icon-plus"
        @click="showEditDialog = true; $store.commit('setPkg', {})"
        v-if="$hasPerm('project.pkg:add')">
        添加知识包
      </el-button>
    </div>

    <el-table
      v-loading="tableLoading"
      element-loading-text="数据加载中..."
      :data="packages"
      class="table-style">
      <el-table-column prop="code">
        <template slot="header" slot-scope="scope">
          编码
          <el-tooltip content="调用时标识当前知识包" placement="top">
            <i class="el-icon-info"></i>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column label="名称" prop="name">
        <template slot-scope="scope">
          <span>{{scope.row.name}}</span>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述"></el-table-column>
      <el-table-column prop="createTime" label="创建时间"></el-table-column>
      <el-table-column label="操作">
        <template slot-scope="scope">
          <el-button @click.stop="showBaseLine(scope.row)" v-if="$hasPerm('project.pkg.baseline:view')" type="text">版本管理</el-button>
          <el-button @click.stop="edit(scope.row)" v-if="$hasPerm('project.pkg:edit')" type="text">编辑</el-button>
          <el-button @click.stop="del(scope.row)" v-if="$hasPerm('project.pkg:del')" type="text">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <edit-dialog :active.sync="showEditDialog" @submit="loadPackages"></edit-dialog>
    <test-dialog :active.sync="showTestDialog" @publish="loadPackages"></test-dialog>
    <baseline-dialog
      v-if="showbaseLineDialog"
      :active.sync="showbaseLineDialog"
      :baselineData="baselineRowData"
      @closed="loadPackages"
      @submit="createMicro">
    </baseline-dialog>
  </div>
</template>

<script>
import editDialog from "./edit-dialog";
import testDialog from "./test-dialog";
import baselineDialog from "./baseline-dialog";

export default {
  name: "index",
  components: {
    "edit-dialog": editDialog,
    "test-dialog": testDialog,
    "baseline-dialog": baselineDialog
  },
  data() {
    return {
      tableLoading: false,
      packages: [],
      showEditDialog: false,
      showAssetsDialog: false,
      showTestDialog: false,
      showbaseLineDialog: false,
      permissionForm: {
        show: false,
        accounts: [],
        accountUuid: "",
        resources: [],
        loading: false,
        all: false
      },
      editingIndex: null,
      baselineRowData:null
    };
  },
  mounted() {
    this.loadPackages();
    this.$store.dispatch("loadBoms", { projectUuid: this.$route.params.uuid });
  },
  methods: {
    edit(row) {
      this.showEditDialog = true;
      this.$store.commit("setPkg", row);
    },
    showBaseLine(row){
      this.baselineRowData = row
      this.showbaseLineDialog = true
    },
    loadPackages() {
      this.tableLoading = true;
      this.$axios.get("/knowledgepackage", {
          params: {
            projectUuid: this.$route.params.uuid
          }
        }).then(res => {
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
            });
            this.packages = res.data.data;
            this.$store.commit("setPkgs", this.packages);
            let arr = [];
            res.data.data.forEach(pkg => {
              arr.push({ ...pkg, r: false });
            });
            this.permissionForm.resources = arr;
          }
        }).then(() => {
          this.tableLoading = false;
        });
    },
    del(row) {
      this.$confirm(`确认删除?`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(() => {
        this.tableLoading = true;
        this.$axios.post("/knowledgepackage/delete",
            this.$qs.stringify({
              uuid: row.uuid
            })
          ).then(res => {
            if (res.data.code == 0) {
              this.loadPackages();
              this.$message.success("删除成功")
            } else {
              this.$message.error(res.data.msg ? res.data.msg : "删除失败");
            }
          }).catch(() => {
            this.$message.error("删除失败");
          }).then(() => {
            this.tableLoading = false;
          });
      });
    },
    addRule(row) {
      this.showAssetsDialog = true;
      this.$store.commit("setPkg", row);
    },
    test(row) {
      this.showTestDialog = true;
      this.$store.commit("setPkg", row);
    },
    changePermissionAccount(uuid) {
      this.permissionForm.loading = true;
      this.$axios.post("/permission/accounthas",
          this.$qs.stringify({
            accountUuid: uuid,
            dataType: 1,
            dataUuid: this.$route.params.uuid
          })
        ).then(res => {
          if (res.data.code == 0) {
            let count = 0;
            this.permissionForm.resources.forEach(resource => {
              let r = false;
              res.data.data.some(e => {
                if (
                  resource.uuid == e.resourceId &&
                  !this.$utils.isBlank(e.permission)
                ) {
                  r = true;
                  count++;
                  return true;
                }
              });
              resource.r = r;
            });
            this.permissionForm.all =
              count == this.permissionForm.resources.length;
          }
        }).catch(() => {
          this.$message.error("操作失败");
        }).then(() => {
          this.permissionForm.loading = false;
        });
    },
    submitPermission(resource) {
      if (this.$utils.isBlank(this.permissionForm.accountUuid)) {
        this.$message.warning("请选择用户");
        resource.r = !resource.r;
        return;
      }
      this.doSubmitPermission(resource.uuid, resource.r ? "r" : "");
    },
    doSubmitPermission(resourceId, permission) {
      this.$axios
        .post(
          "/permission/authorization",
          this.$qs.stringify({
            accountUuid: this.permissionForm.accountUuid,
            resourceId: resourceId,
            type: 3,
            permission: permission
          })
        )
        .then(res => {
          if (res.data.code != 0) {
            this.$message.error(res.data.msg ? res.data.msg : "操作失败");
          }
        });
    },
    createMicro(microUuid) {
      this.$set(this.packages[this.editingIndex], 'microUuid', microUuid)
    }
  }
};
</script>

<style scoped>
</style>
