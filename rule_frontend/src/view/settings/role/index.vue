<template>
  <div>
    <el-card>
      <el-tabs v-model="activeName" @tab-click="clickTab">
        <el-tab-pane label="角色管理" name="first">
          <div>
            <div class="toolbar">
              <el-button
                v-if="$hasPerm('role:add')"
                type="primary"
                icon="el-icon-plus"
                @click="handleAdd">
                添加角色
              </el-button>
            </div>

            <el-table
              v-loading="tableLoading"
              element-loading-text="数据加载中..."
              :data="roles"
              highlight-current-row>
              <el-table-column label="角色编码" prop="code"></el-table-column>
              <el-table-column label="角色名称" prop="name"></el-table-column>
              <el-table-column label="操作">
                <template slot-scope="scope">
                  <el-button type="text" v-if="$hasPerm('role:viewuser')" @click="showUsers(scope.row)">用户</el-button>
                  <el-button
                    v-if="$hasPerm('role:edit')"
                    type="text"
                    @click="editRole(scope.row)">
                    编辑
                  </el-button>
                  <el-popconfirm
                    title="确定删除？"
                    @onConfirm="delRole(scope.row)"
                    style="margin-left: 10px;"
                    v-if="$hasPerm('role:delete') && canDelete(scope.row.code)">
                    <el-button type="text" slot="reference">删除</el-button>
                  </el-popconfirm>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <el-tab-pane label="权限管理" name="second">
          <permisson v-if="activeName == 'second'" />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog
      :title="this.roleForm.id > 0 ? '编辑角色' : '新增角色'"
      :top="$dialogTop"
      :visible.sync="showRoleDialog"
      @opened="$refs.name.focus()"
      @closed="closeRoleDialog"
      width="1000px"
      :close-on-click-modal="false">
      <el-form
        :model="roleForm"
        :rules="roleForm.id > 0 ? editRules : addRules"
        ref="roleForm"
        label-width="80px"
        inline
        @submit.native.prevent>
        <el-form-item prop="name" label="角色名称">
          <el-input ref="name" v-model.trim="roleForm.name"></el-input>
        </el-form-item>
        <el-form-item prop="code" label="角色编码" v-if="roleForm.id == 0">
          <el-input v-model.trim="roleForm.code"></el-input>
        </el-form-item>
      </el-form>
      <div>
        <el-divider>权限设置</el-divider>
        <el-row
          :gutter="16"
          v-for="(group, index) in permissions"
          :key="index">
          <el-divider v-if="index > 0" />
          <el-col :span="3" class="permissionName">{{ group.module }}：</el-col>
          <el-col :span="20">
            <el-checkbox
              v-model="group.checkAll"
              :indeterminate="group.isIndeterminate"
              @change="val => handleGroupCheckAll(val, group)">
              全选
            </el-checkbox>
            <el-checkbox-group
              v-model="group.checked"
              @change="val => handleGroupCheck(val, group)">
              <el-checkbox
                v-for="permission in group.permissions"
                :key="permission.id"
                :label="permission.id">
                {{permission.name}}
              </el-checkbox>
            </el-checkbox-group>
          </el-col>
        </el-row>
      </div>
      <div slot="footer">
        <el-button @click="showRoleDialog = false" icon="el-icon-error">取 消</el-button>
        <el-button
          type="primary"
          @click="handleOkEdit"
          icon="el-icon-success"
          :loading="confirmLoading">
          {{confirmLoading ? '提交中...' : '确 定'}}
        </el-button>
      </div>
    </el-dialog>

    <user-list :show.sync="listingUser.show" :role-id="listingUser.roleId"></user-list>
  </div>
</template>
<script>
import permisson from "../permission/index";
import UserList from "../user/user-list";

export default {
  components: {
    UserList,
    permisson,
  },
  data() {
    return {
      tableLoading: false,
      roles: [],
      permissions: [],
      showRoleDialog: false,
      roleForm: {
        id: 0,
        code: '',
        name: ''
      },
      activeName: 'first',
      confirmLoading: false,
      editRules: {
        name: [{ required: true, message: "请输入角色名称", trigger: "blur" }],
      },
      addRules: {
        name: [{ required: true, message: "请输入角色名称", trigger: "blur" }],
        code: [{ required: true, message: "请输入角色编码", trigger: "blur" }],
      },
      listingUser: {
        show: false,
        roleId: 0
      }
    };
  },
  mounted() {
    this.loadRoles()
    this.loadPermissions();
  },
  methods: {
    loadRoles() {
      this.tableLoading = true
      this.$axios.get('/role').then(res => {
        this.tableLoading = false
        if (res.data.code == 0) {
          this.roles = res.data.data
        } else {
          this.$message.error(res.data.msg ? res.data.msg : '系统异常')
        }
      }).catch(() => {
        this.$message.error('系统异常')
        this.tableLoading = false
      })
    },
    loadPermissions() {
      this.$axios.get('/role/permission?pretty').then(res => {
        if (res.data.code == 0) {
          this.permissions = res.data.data
        }
      })
    },
    canDelete(code) {
      return ['SystemAdmin', 'OrgAdmin', 'ModelingUser'].indexOf(code) == -1
    },
    //增加角色
    handleAdd() {
      this.roleForm = {
        id: 0,
        code: '',
        name: ''
      }
      this.permissions.forEach(group => {
        this.$set(group, 'checked', [])
        this.$set(group, 'checkAll', false)
        this.$set(group, 'isIndeterminate', false)
      })
      this.showRoleDialog = true
    },
    //编辑角色
    editRole(data) {
      this.roleForm = {
        id: data.id,
        name: data.name,
        code: data.code
      }
      this.permissions.forEach(group => {
        let checked = []
        data.permissions.forEach(e => {
          if (e.code == '*') {
            group.permissions.forEach(gp => {
              checked.push(gp.id)
            })
          } else if (group.module == e.module) {
            checked.push(e.id)
          }
        })
        this.$set(group, 'checked', checked)
        const checkedCount = checked.length
        const checkAll = checkedCount === group.permissions.length
        const isIndeterminate = checkedCount > 0 && checkedCount < group.permissions.length
        this.$set(group, 'checkAll', checkAll)
        this.$set(group, 'isIndeterminate', isIndeterminate)
      })
      this.showRoleDialog = true
    },
    handleOkEdit(e) {
      this.$refs.roleForm.validate(valid => {
        if (valid) {
          this.confirmLoading = true
          const url = this.roleForm.id > 0 ? '/role/edit' : '/role/add'
          let params = {
            name: this.roleForm.name,
            code: this.roleForm.code
          }
          if (this.roleForm.id > 0) {
            params.id = this.roleForm.id
          }
          let permissionIdArr = []
          this.permissions.forEach(group => {
            group.checked.forEach(checkId => {
              permissionIdArr.push(checkId)
            })
          })
          params.permissionIds = permissionIdArr.join(',')

          this.$axios.post(url, this.$qs.stringify(params)).then(res => {
            this.confirmLoading = false
            if (res.data.code == 0) {
              this.$message.success('保存成功')
              this.showRoleDialog = false
              this.loadRoles()
            } else {
              this.$message.error(res.data.msg ? res.data.msg : '保存失败')
            }
          }).catch(() => {
            this.$message.error('保存失败')
            this.confirmLoading = false
          })
        }
      });
    },
    delRole(row) {
      this.tableLoading = true;
      this.$axios.post("/role/delete",this.$qs.stringify({ id: row.id })).then((res) => {
        this.tableLoading = false
        if (res.data.code == 0) {
          this.$message.success("删除成功");
          this.loadRoles();
        } else {
          this.$message.error(res.data.msg ? res.data.msg : "删除失败");
        }
      }).catch(() => {
        this.$message.error("删除失败");
        this.tableLoading = false
      })
    },
    closeRoleDialog() {
      this.$refs.roleForm.resetFields()
    },
    getGroupCheckedCount(group) {
      let checkedCount = 0
      group.checked.forEach(e => {
        if (e > 0) {
          checkedCount++
        }
      })
      return checkedCount
    },
    handleGroupCheckAll(val, group) {
      group.checked = []
      if (val) {
        group.permissions.forEach(e => {
          group.checked.push(e.id)
        })
      }
      group.isIndeterminate = false
    },
    handleGroupCheck(val, group) {
      const checkedCount = group.checked.length
      group.checkAll = checkedCount == group.permissions.length
      group.isIndeterminate = checkedCount > 0 && checkedCount < group.permissions.length
    },
    clickTab(tab) {
      if (tab.name == 'first') {
        this.loadRoles()
      }
    },
    showUsers(row) {
      this.listingUser.show = true
      this.listingUser.roleId = row.id
    }
  }
};
</script>

<style>
.permissionName {
  font-weight: bold;
  width: 10%;
}
</style>
