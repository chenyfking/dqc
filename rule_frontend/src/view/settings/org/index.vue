<template>
  <div>
    <el-card>
      <div class="toolbar">
        <el-button
          class="toolbar"
          type="primary"
          icon="el-icon-plus"
          @click="showAddDialog = true"
          v-if="$hasPerm('org:add')">
          添加机构
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

      <el-table v-loading="tableLoading" element-loading-text="数据加载中..." :data="list">
        <el-table-column prop="name" label="机构名称"></el-table-column>
        <el-table-column prop="updateTime" label="更新时间"></el-table-column>
        <el-table-column prop="createTime" label="创建时间"></el-table-column>
        <el-table-column label="操作">
          <template slot-scope="scope">
            <el-button type="text" v-if="$hasPerm('org:viewuser')" @click="showUsers(scope.row)">用户</el-button>
            <el-button type="text" v-if="$hasPerm('org:edit')" @click="edit(scope.row)">编辑</el-button>
            <el-popconfirm
              title="确定删除机构？"
              @onConfirm="del(scope.row)"
              v-if="$hasPerm('org:del')"
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
    </el-card>

    <el-dialog
      :title="form.uuid ? '编辑机构' : '添加机构'"
      width="30%"
      :visible.sync="showAddDialog"
      @opened="$refs.input.focus()"
      @closed="closed"
      :close-on-click-modal="false">
      <el-form :model="form" :rules="rules" ref="form" label-width="80px" @submit.native.prevent>
        <el-form-item label="机构名称" prop="name">
          <el-input
            v-model.trim="form.name"
            ref="input"
            :maxlength="40"
            show-word-limit
            @keyup.enter.native="submit"
            placeholder="请填写机构名称">
          </el-input>
        </el-form-item>
      </el-form>
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

    <user-list :show.sync="listingUser.show" :org-uuid="listingUser.orgUuid"></user-list>
  </div>
</template>

<script>
import UserList from "../user/user-list";

export default {
  name: "index",
  components: {UserList},
  data() {
    return {
      tableLoading: false,
      showAddDialog: false,
      list: [],
      addLoading: false,
      form: {
        uuid: "",
        name: ""
      },
      rules: {
        name: [
          { required: true, message: "请填写机构名称", trigger: "blur" },
          { max: 40, message: "长度不超过40个字符", trigger: "blur" }
        ]
      },
      total: 0,
      page: 1,
      searchName: "",
      listingUser: {
        show: false,
        orgUuid: ''
      }
    };
  },
  mounted() {
    this.initData();
  },
  methods: {
    toPage(page) {
      this.page = page;
      this.initData();
    },
    initData() {
      this.tableLoading = true;
      this.$axios.get("/org", {
        params: { page: this.page, name: this.searchName }
      }).then(res => {
        if (res.data.code == 0) {
          res.data.data.forEach(e => {
            if (e.createTime) {
              e.createTime = this.$moment(e.createTime).format("YYYY-MM-DD HH:mm");
            }
            if (e.updateTime) {
              e.updateTime = this.$moment(e.updateTime).format("YYYY-MM-DD HH:mm");
            }
          });
          this.list = res.data.data;
          this.total = res.data.total;
        }
        this.tableLoading = false;
      }).catch(() => {
        this.tableLoading = false;
      });
    },
    //搜索
    search() {
      this.toPage(1);
    },
    edit(row) {
      this.form.uuid = row.uuid;
      this.form.name = row.name;
      this.showAddDialog = true;
    },
    del(row) {
      this.tableLoading = true
      this.$axios.post("/org/delete", this.$qs.stringify({
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
    },
    closed() {
      this.$refs.form.resetFields()
      this.form.uuid = ''
    },
    submit() {
      this.$refs.form.validate(valid => {
        if (valid) {
          this.addLoading = true;
          const url = this.form.uuid ? "/org/edit" : "/org/add";
          this.$axios.post(url, this.$qs.stringify(this.form)).then(res => {
            this.addLoading = false;
            if (res.data.code == 0) {
              this.$message.success("操作成功");
              this.initData();
              this.showAddDialog = false;
            } else {
              this.$message.error(res.data.msg ? res.data.msg : "操作失败");
            }
          }).catch(() => {
            this.$message.error("操作失败");
            this.addLoading = false;
          })
        }
      });
    },
    showUsers(row) {
      this.listingUser.show = true
      this.listingUser.orgUuid = row.uuid
    }
  }
};
</script>

<style scoped>
</style>
