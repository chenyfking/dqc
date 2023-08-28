<template>
  <div>
    <el-card>
      <el-table v-loading="tableLoading" element-loading-text="数据加载中..." :data="list">
        <el-table-column prop="baseUrl" label="地址"></el-table-column>
        <el-table-column label="是否启用" align="center">
          <template slot-scope="scope">
            <i class="el-icon-success" style="color: green; font-size: 18px;" v-if="!scope.row.disabled"></i>
            <i class="el-icon-error" style="color: red; font-size: 18px;" v-else></i>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间"></el-table-column>
        <el-table-column label="操作">
          <template slot-scope="scope">
            <el-button
              type="text"
              v-if="$hasPerm('client:edit')"
              @click="disable(scope.row)">
              {{scope.row.disabled ? '启用' : '禁用'}}
            </el-button>
            <el-popconfirm title="确定删除节点？"
                           @onConfirm="del(scope.row)"
                           v-if="$hasPerm('client:del')"
                           style="margin-left: 10px;">
              <el-button type="text" slot="reference">删除</el-button>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
export default {
  name: "index",
  data() {
    return {
      tableLoading: false,
      list: []
    };
  },
  mounted() {
    this.initData();
  },
  methods: {
    initData() {
      this.tableLoading = true;
      this.$axios.get("/client").then(res => {
        this.tableLoading = false
        if (res.data.code == 0) {
          res.data.data.forEach(e => {
            if (e.createTime) {
              e.createTime = this.$moment(e.createTime).format("YYYY-MM-DD HH:mm");
            }
          });
          this.list = res.data.data;
        }
      }).catch(() => {
        this.tableLoading = false
      });
    },
    disable(row) {
      this.tableLoading = true;
      const url = row.disabled ? '/client/enable' : '/client/disable'
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
      }).catch(() => {
        this.$message.error("操作失败");
        this.tableLoading = false;
      });
    },
    del(row) {
      this.tableLoading = true;
      this.$axios.post("/client/delete", this.$qs.stringify({
        uuid: row.uuid
      })).then(res => {
        this.tableLoading = false;
        if (res.data.code == 0) {
          this.$message.success("删除成功");
          this.initData();
        } else {
          this.$message.error(res.data.msg ? res.data.msg : "删除失败");
        }
      }).catch(() => {
        this.$message.error("删除失败");
        this.tableLoading = false;
      });
    }
  }
};
</script>

<style scoped>
</style>
