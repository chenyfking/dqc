<template>
  <div>
    <el-card>
      <el-alert type="info" show-icon :title="'在线人数：' + total" :closable="false" class="toolbar"></el-alert>
      <el-table
        v-loading="tableLoading"
        element-loading-text="数据加载中..."
        @sort-change="sortChange"
        :data="onlines">
        <el-table-column prop="user.realname" label="在线账号"></el-table-column>
        <el-table-column sortable="custom" prop="loginTime" label="登录时间"></el-table-column>
        <el-table-column sortable="custom" prop="onlineTime" label="在线时长"></el-table-column>
        <el-table-column prop="user.lastLoginIP" label="登录IP"></el-table-column>
        <el-table-column label="操作" v-if="$hasPerm('login:offline')">
          <template slot-scope="scope">
            <el-popconfirm
              v-if="$store.getters.userUuid != scope.row.user.uuid"
              title="确认强制下线？"
              @onConfirm="forceLogout(scope.row.user)">
              <el-button slot="reference" type="text">强制下线</el-button>
            </el-popconfirm>
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
  </div>
</template>

<script>
export default {
  name: "index",
  data() {
    return {
      tableLoading: false,
      onlines: [],
      tablePage: 1,
      total: 0,
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
    toPage(page) {
      this.tablePage = page;
      this.initData();
    },
    initData() {
      this.tableLoading = true;
      this.$axios.get("/user/onlines", {
        params: {
          page: this.tablePage,
          sortField: this.sortOption.sortField,
          sortDirection: this.sortOption.sortDirection
        }
      }).then(res => {
        this.tableLoading = false;
        if (res.data.code == 0) {
          res.data.data.forEach(e => {
            if (e.loginTime) {
              e.loginTime = this.$moment(e.loginTime).format("YYYY-MM-DD HH:mm:ss");
            }
          });
          this.onlines = res.data.data;
          this.total = res.data.total;
        }
      }).catch(() => {
        this.tableLoading = false;
      });
    },
    sortChange(data) {
      this.sortOption.sortField = data.prop;
      this.sortOption.sortDirection = data.order == "ascending" ? "asc" : "desc";
      this.initData();
    },
    forceLogout(user) {
      this.tableLoading = true;
      this.$axios.post("/user/forcelogout", this.$qs.stringify({
        uuid: user.uuid,
        loginIp: user.lastLoginIP
      })).then(res => {
        this.tableLoading = false;
        if (res.data.code == 0) {
          this.$message.success("操作成功")
          this.initData();
        } else {
          this.$message.error(res.data.msg ? res.data.msg : "操作失败")
        }
      }).catch(() => {
        this.$message.error("操作失败")
        this.tableLoading = false;
      });
    }
  }
};
</script>

<style scoped>
</style>
