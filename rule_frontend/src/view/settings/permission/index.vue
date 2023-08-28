<template>
  <div>
    <div v-for="group in groups" :key="group.module">
      <p @click="group.open = !group.open" class="openTab">
        <i class="el-icon-plus" v-if="!group.open"></i>
        <i class="el-icon-minus" v-else></i>
        {{group.module}}
      </p>
      <el-divider />
      <div v-if="group.open" class="tabCont">
        <el-table :data="group.permissions" style="width: 100%;" max-height="250">
          <el-table-column label="权限" prop="name" fixed width="150"></el-table-column>
          <el-table-column v-for="(role, j) in roles" :key="role.code">
            <template slot="header">
              <span>{{role.name}}</span>
            </template>
            <template slot-scope="scope">
              <el-checkbox
                @change="e => onChange(scope.row.roles[j].id, scope.row.id, e)"
                :disabled="!canEditRole"
                :checked="scope.row.roles[j].checked">
              </el-checkbox>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>
<script>
export default {
  data() {
    return {
      groups: [],
      permissionGroups: [],
      roles: []
    };
  },
  mounted() {
    this.init();
  },
  computed: {
    canEditRole() {
      return this.$hasPerm('role:edit')
    }
  },
  methods: {
    init() {
      this.$axios.get('/role/permission?pretty').then(res => {
        if (res.data.code == 0) {
          this.permissionGroups = res.data.data
          this.$axios.get('/role').then(res => {
            if (res.data.code == 0) {
              this.roles = res.data.data
              this.merge()
            } else {
              this.$message.error(res.data.msg ? res.data.msg : '系统异常')
            }
          }).catch(() => {
            this.$message.error('系统异常')
          })
        } else {
          this.$message.error(res.data.msg ? res.data.msg : '系统异常')
        }
      }).catch(() => {
        this.$message.error('系统异常')
      })
    },
    merge() {
      this.permissionGroups.forEach(group => {
        let item = {
          module: group.module,
          open: false,
          permissions: []
        }
        group.permissions.forEach(gp => {
          let perm = {
            id: gp.id,
            name: gp.name,
            roles: []
          }
          this.roles.forEach(role => {
            let checked = false
            role.permissions.some(rp => {
              if (rp.code == '*' || rp.id == gp.id) {
                checked = true
                return true
              }
            })
            perm.roles.push({id: role.id, checked})
          })
          item.permissions.push(perm)
        })
        this.groups.push(item)
      })
    },
    onChange(roleId, permissionId, checked) {
      const params = {roleId, permissionId}
      const url = checked ? '/role/addpermission' : '/role/delpermission'
      this.$axios.post(url, this.$qs.stringify(params)).then(res => {
        if (res.data.code == 0) {
          this.$message.success('编辑成功')
        } else {
          this.$message.error(res.data.msg ? res.data.msg : '系统异常')
        }
      }).catch(() => {
        this.$message.error('系统异常')
      })
    }
  }
};
</script>
<style scoped>
.openTab {
  cursor: pointer;
  height: 50px;
  line-height: 50px;
  font-size: 14px;
  margin: 0;
  font-weight: bold;
  color: #666;
}
.openTab:hover {
  background: #f5f7fa;
}
.tabCont {
  margin-bottom: 28px;
}
</style>
