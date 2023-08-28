<template>
  <el-card>
    <div class="toolbar">
      <el-button
        v-if="$hasPerm('token:add')"
        type="primary"
        icon="el-icon-plus"
        @click="showAddDialog = true">
        添加令牌
      </el-button>
    </div>

    <el-table
      v-loading="tableLoading"
      element-loading-text="数据加载中..."
      :data="tableData">
      <el-table-column prop="name" label="名称" show-overflow-tooltip></el-table-column>
      <el-table-column label="全部服务" align="center">
        <template slot-scope="scope">
          <i class="el-icon-success" style="color: green; font-size: 18px;" v-if="scope.row.all"></i>
          <i class="el-icon-error" style="color: red; font-size: 18px;" v-else></i>
        </template>
      </el-table-column>
      <el-table-column prop="token" width="300" label="token"></el-table-column>
      <el-table-column prop="creatorName" label="创建人"></el-table-column>
      <el-table-column prop="createTime" label="创建时间"></el-table-column>
      <el-table-column prop="updateTime" label="更新时间"></el-table-column>
      <el-table-column label="操作">
        <template slot-scope="scope">
          <el-button
            type="text"
            @click="edit(scope.row)"
            v-if="$hasPerm('token:edit')">
            编辑
          </el-button>
          <el-popconfirm
            title="确定删除令牌？"
            @onConfirm="del(scope.row)"
            v-if="$hasPerm('token:del')"
            style="margin-left: 10px;">
            <el-button type="text" slot="reference">删除</el-button>
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

    <el-dialog
      :title="form.uuid ? '编辑令牌' : '添加令牌'"
      :top="$dialogTop"
      :visible.sync="showAddDialog"
      @open="openAddDialog"
      @opened="$refs.nameInput.focus()"
      @closed="closed()"
      width="800px"
      :close-on-click-modal="false">
      <el-form
        :model="form"
        :rules="rules"
        ref="form"
        label-width="80px"
        inline
        @submit.native.prevent>
        <el-form-item prop="name" label="令牌名称">
          <el-input ref="nameInput" v-model.trim="form.name" :maxlength="40" show-word-limit></el-input>
        </el-form-item>
        <el-form-item prop="all" label="全部服务">
          <el-switch v-model="form.all"></el-switch>
        </el-form-item>
      </el-form>
      <div v-if="!form.all">
        <el-divider>服务设置</el-divider>
        <el-transfer
          v-loading="microLoading"
          element-loading-text="数据加载中..."
          filterable
          filter-placeholder="搜索"
          :titles="['未选择', '已选择']"
          :button-content="['选择', '取消']"
          v-model="form.microUuids"
          :data="micros">
        </el-transfer>
      </div>
      <div slot="footer">
        <el-button @click="showAddDialog = false" icon="el-icon-error">取 消</el-button>
        <el-button
          type="primary"
          @click="submit"
          icon="el-icon-success"
          :loading="addLoading">
          {{addLoading ? '提交中...' : '确 定'}}
        </el-button>
      </div>
    </el-dialog>
  </el-card>
</template>
<script>
import { mapState } from "vuex";
export default {
  data() {
    return {
      tableData: [],
      tableLoading: false,
      total: 0,
      page: 1,
      showAddDialog: false,
      addLoading: false,
      form: {
        uuid: '',
        name: '',
        all: false,
        microUuids: []
      },
      rules: {
        name: [
          { required: true, message: "请填写令牌名称", trigger: "blur" },
          { max: 40, message: "长度不超过40个字符", trigger: "blur" }
        ]
      },
      microLoading: false,
      micros: []
    }
  },
  mounted() {
    this.init();
  },
  methods: {
    init() {
      this.tableLoading = true;
      this.$axios.get("/token", {
        params: {
          page: this.page
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
    toPage(page) {
      this.page = page;
      this.init();
    },
    closed() {
      this.form.uuid = ''
      this.form.name = ''
      this.form.all = false
      this.form.microUuids = []
    },
    submit() {
      this.$refs.form.validate(valid => {
        if (valid) {
          const url = this.form.uuid ? '/token/edit' : '/token/add'

          const params = {
            name: this.form.name
          }
          if (this.form.all) {
            params.all = true
          } else {
            params.microUuids = this.form.microUuids.join(',')
          }
          if (this.form.uuid) {
            params.uuid = this.form.uuid
          }
          this.addLoading = true;
          this.$axios.post(url, this.$qs.stringify(params)).then(res => {
            this.addLoading = false;
            if (res.data.code == 0) {
              this.$message.success("操作成功");
              this.showAddDialog = false;
              this.init();
            } else {
              this.$message.error(res.data.msg ? res.data.msg : "操作失败");
            }
          }).catch(() => {
            this.$message.error("操作失败");
            this.addLoading = false;
          });
        }
      });
    },
    //删除
    del(row) {
      this.tableLoading = true;
      this.$axios.post("/token/delete", this.$qs.stringify({
        uuid: row.uuid,
        token: row.token
      })).then(res => {
        this.tableLoading = false;
        if (res.data.code == 0) {
          this.$message.success("操作成功");
          this.init();
        } else {
          this.$message.error(res.data.msg ? res.data.msg : "删除失败");
        }
      }).catch(() => {
        this.$message.error("删除失败");
        this.tableLoading = false;
      });
    },
    openAddDialog() {
      this.microLoading = true
      this.micros = []
      this.$axios.get('/token/micro').then(res => {
        this.microLoading = false
        if (res.data.code == 0) {
          res.data.data.forEach(e => {
            this.micros.push({
              key: e.uuid,
              label: e.name
            })
          })
        }
      }).catch(() => {
        this.microLoading = false
      })
    },
    edit(row) {
      this.form.uuid = row.uuid
      this.form.name = row.name
      this.form.all = row.all
      this.form.microUuids = row.microUuids
      this.showAddDialog = true
    }
  }
};
</script>

<style scoped>
  .el-transfer {
    display: flex;
    align-items: center;
    justify-content: space-around;
  }
</style>
