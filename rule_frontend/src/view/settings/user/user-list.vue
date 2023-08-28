<template>
  <el-dialog
    title="用户列表"
    width="60%"
    :visible="show"
    @update:visible="close"
    @open="open"
    @closed="closed">
    <div class="toolbar pull-left">
      <el-input
        v-model.trim="keywords"
        @keyup.enter.native="loadUsers"
        placeholder="搜索"
        class="search">
        <i slot="suffix" class="el-input__icon el-icon-search" @click="loadUsers"></i>
      </el-input>
    </div>
    <el-table v-loading="tableLoading" element-loading-text="数据加载中..." :data="users">
     <el-table-column v-if="$hasPerm('user:detail')" label="用户名">
        <template slot-scope="scope">
          <!-- <span v-if="!scope.row.disabled">{{scope.row.username}}</span> -->
          <el-button v-if="!scope.row.disabled" type="text" @click="showUserProfile(scope.row)">{{scope.row.username}}</el-button>
          <el-tooltip v-else placement="top" content="已禁用">
            <el-button type="text" style="text-decoration: line-through; color: red;"  @click="showUserProfile(scope.row)">{{scope.row.username}}</el-button>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column v-else prop="username" label="用户名">
        <template slot-scope="scope">
          <span v-if="!scope.row.disabled">{{scope.row.username}}</span>
          <el-tooltip v-else placement="top" content="已禁用">
            <span style="text-decoration: line-through; color: red;">{{scope.row.username}}</span>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column prop="realname" label="真实姓名"></el-table-column>
      <el-table-column prop="lastLoginTime" label="最后登录"></el-table-column>
      <el-table-column prop="expiredTime" label="过期时间"></el-table-column>
      <el-table-column prop="org.name" label="所属机构" show-overflow-tooltip v-if="!orgUuid"></el-table-column>
      <el-table-column v-if="$hasPerm('user:viewlog')" label="操作">
        <template slot-scope="scope">
          <el-button type="text" @click="showLogs(scope.row)">操作日志</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      background
      layout="total, prev, pager, next, jumper"
      :total="total"
      hide-on-single-page
      :current-page.sync="page"
      @current-change="loadUsers">
    </el-pagination>

    <user-logs-dialog :show.sync="userLogs.show" :user-uuid="userLogs.userUuid"></user-logs-dialog>
    <user-profile :show.sync="userProfile.show" :user-uuid="userProfile.userUuid"></user-profile>
  </el-dialog>

</template>

<script>

  import UserLogsDialog from "../user/user-logs-dialog";
  import UserProfile from '../user/user-profile';

  export default {
    name: "user-list",
    components: {
      UserLogsDialog,
      UserProfile
    },
    props: {
      show: Boolean,
      orgUuid: String,
      roleId: Number
    },
    data() {
      return {
        tableLoading: false,
        users: [],
        total: 0,
        page: 1,
        keywords: '',
        userLogs: {
          show: false,
          userUuid: "",
        },
        userProfile: {
          show: false,
          userUuid: "",
        }
      }
    },
    mounted() {

    },
    methods: {
      open() {
        this.loadUsers()
      },
      loadUsers() {
        let req
        if (this.orgUuid) {
          req = this.loadUsersByOrg()
        } else if (this.roleId) {
          req = this.loadUsersByRole()
        }

        if (!req) return

        this.tableLoading = true
        this.users = []
        req.then(res => {
          this.tableLoading = false
          if (res.data.code == 0) {
            res.data.data.forEach((e) => {
              if (e.createTime) {
                e.createTime = this.$moment(e.createTime).format("YYYY-MM-DD HH:mm");
              }
              if (e.lastLoginTime) {
                e.lastLoginTime = this.$moment(e.lastLoginTime).format("YYYY-MM-DD HH:mm");
              }
              e.org = e.org ? e.org : { name: "", uuid: "" };
            });
            this.users = res.data.data;
            this.total = res.data.total;
          }
        }).catch(() => {
          this.tableLoading = false
        })
      },
      loadUsersByOrg() {
        return this.$axios.get('/org/users', {
          params: {
            orgUuid: this.orgUuid,
            page: this.page,
            keywords: this.keywords
          }
        })
      },
      loadUsersByRole() {
        return this.$axios.get('/role/users', {
          params: {
            roleId: this.roleId,
            page: this.page,
            keywords: this.keywords
          }
        })
      },
      close() {
        this.$emit('update:show', false)
      },
      closed() {
        this.users = []
        this.page = 1
      },
      showLogs(row) {
        this.userLogs.show = true
        this.userLogs.userUuid = row.uuid
      },
      showUserProfile(row) {
        this.userProfile.show = true
        this.userProfile.userUuid = row.uuid
      }
    }
  }
</script>

<style scoped>

</style>
