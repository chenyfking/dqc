<template>
  <el-card>
    <div class="toolbar">
      <el-button
        type="primary"
        icon="el-icon-plus"
        @click="showAddDialog = true"
        v-if="$hasPerm('user:add')">
        添加用户
      </el-button>
      <div class="pull-right">
        <el-input
          v-model.trim="searchWords"
          @keyup.enter.native="search"
          placeholder="搜索"
          class="search">
          <i slot="suffix" class="el-input__icon el-icon-search" @click="search"></i>
        </el-input>
      </div>
    </div>

    <el-table
      v-loading="tableLoading"
      element-loading-text="数据加载中..."
      highlight-current-row
      :data="users">
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
      <el-table-column prop="realname" label="真实姓名" show-overflow-tooltip></el-table-column>
      <el-table-column prop="lastLoginTime" label="最后登录"></el-table-column>
      <el-table-column prop="createTime" label="创建时间"></el-table-column>
      <el-table-column prop="expiredTime" label="过期时间"></el-table-column>
      <el-table-column prop="org.name" label="所属机构" show-overflow-tooltip></el-table-column>
      <el-table-column label="操作">
        <template slot-scope="scope">
          <el-button type="text" @click="editUser(scope.row)" v-if="$hasPerm('user:edit')">编辑</el-button>
          <dropdown
            @command="moreOperation"
            class="moreOperation"
            v-if="$hasAnyPerm('user:edit:pwd', 'user:edit:validation', 'user:del')">
            <span class="el-dropdown-link">
              更多<i class="el-icon-arrow-down el-icon--right"></i>
            </span>
            <dropdown-menu slot="dropdown">
              <dropdown-item
                v-if="$hasPerm('user:edit:validation')"
                :command="{row: scope.row, op: 'enable'}"
                :icon="!scope.row.disabled ? 'el-icon-lock' : 'el-icon-unlock'">
                {{!scope.row.disabled ? '禁用' : '启用'}}
              </dropdown-item>
              <dropdown-item
                v-if="$hasPerm('user:del')"
                :command="{row: scope.row, op: 'del'}"
                icon="el-icon-delete"
                divided>
                删除
              </dropdown-item>
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
      :current-page.sync="tablePage"
      @current-change="initData">
    </el-pagination>

    <el-dialog
      :title="form.uuid ? '编辑用户' : '添加用户'"
      :top="$dialogTop"
      :visible.sync="showAddDialog"
      @opened="$refs.realnameInput.focus()"
      :close-on-click-modal="false"
      width="30%"
      @closed="closeAddDialog">
      <el-form
        :model="form"
        :rules="rules"
        ref="form"
        label-width="90px"
        @submit.native.prevent>
        <el-form-item label="真实姓名" prop="realname">
          <el-input
            v-model.trim="form.realname"
            ref="realnameInput"
            :maxlength="20"
            show-word-limit
            @keyup.enter.native="submitAdd"
            placeholder="请填写真实姓名">
          </el-input>
        </el-form-item>
        <el-form-item label="登录用户" prop="username" v-if="form.uuid == ''">
          <el-input
            v-model.trim="form.username"
            :maxlength="20"
            show-word-limit
            @keyup.enter.native="submitAdd"
            placeholder="请填写用户名">
          </el-input>
        </el-form-item>
        <el-form-item label="登录密码" prop="password" :required="form.uuid == ''">
          <el-input
            type="password"
            v-model.trim="form.password"
            show-password
            @keyup.enter.native="submitAdd"
            placeholder="请填写密码">
          </el-input>
        </el-form-item>
        <el-form-item label="所属机构" prop="orgUuid">
          <el-select
            filterable
            remote
            :remote-method="remoteSearchOrg"
            @focus="remoteSearchOrg"
            :loading="remoteLoading"
            loading-text="数据加载中..."
            clearable
            v-model="form.orgUuid"
            placeholder="请选择机构"
            style="width:100%">
            <el-option
              v-for="item in orgs"
              :label="item.name"
              :value="item.uuid"
              :key="item.uuid">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="角色设置" prop="roleIds">
          <el-select
            clearable
            multiple
            v-model="form.roleIds"
            placeholder="请选择角色"
            @focus="remoteSearchRole"
            style="width: 100%;">
            <el-option v-for="item in roles" :label="item.name" :value="item.id" :key="item.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="过期时间" prop="expiredTime">
          <el-date-picker
            v-model="form.expiredTime"
            type="datetime"
            value-format="yyyy-MM-dd HH:mm:ss"
            placeholder="请选择过期时间"
            :picker-options="expiredTimePickerOptions"
            style="width: 100%">
          </el-date-picker>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="showAddDialog = false" icon="el-icon-error">取 消</el-button>
        <el-button
          type="primary"
          @click="submitAdd"
          icon="el-icon-success"
          :loading="addLoading">
          {{addLoading ? '提交中...' : '确 定'}}
        </el-button>
      </div>
    </el-dialog>

    <user-profile :show.sync="userProfile.show" :user-uuid="userProfile.userUuid"></user-profile>
  </el-card>

</template>

<script>
  import UserProfile from '../user/user-profile';

  export default {
    components: {
      UserProfile
    },
  name: "index",
  data() {
    return {
      users: [],
      showAddDialog: false,
      tableLoading: false,
      addLoading: false,
      companies: [],
      searchWords: '',
      total: 0,
      tablePage: 1,
      expiredTimePickerOptions: {
        disabledDate: time => {
          return time.getTime() < new Date().getTime()
        },
        firstDayOfWeek: 1
      },
      form: {
        uuid: '',
        realname: '',
        username: '',
        password: '',
        orgUuid: '',
        roleIds: [],
        expiredTime: ''
      },
      rules: {
        realname: [
          { required: true, message: "请填写真实姓名", trigger: "blur" },
          { max: 20, message: "长度不超过20个字符", trigger: "blur" },
        ],
        username: [
          { required: true, message: "请填写用户名", trigger: "blur" },
          { max: 20, message: "长度不超过20个字符", trigger: "blur" },
          { type: 'string', pattern: /^[a-zA-Z0-9]+$/, message: '只支持英文字母或数字', trigger: 'blur' }
        ],
        password: [
          { min: 6, message: "长度至少6位", trigger: "blur" },
          {
            validator: (rule, value, callback) => {
              if (!this.form.uuid) {
                if (!value) {
                  callback(new Error('请填写密码'))
                } else {
                  callback()
                }
              } else {
                callback()
              }
            }, trigger: 'blur'
          }
        ],
        orgUuid: [
          { required: true, message: "请选择机构", trigger: "change" },
        ],
        expiredTime: [
          {
            validator: (rule, value, callback) => {
              if (value) {
                const now = this.$moment(new Date()).format('YYYY-MM-DD HH:mm:ss')
                if (value <= now) {
                  callback(new Error('过期时间不能小于或等于当前时间'))
                } else {
                  callback()
                }
              } else {
                callback()
              }
            }, trigger: 'blur'
          }
        ]
      },
      orgs: [],
      remoteLoading: false,
      roles: [],
      userProfile: {
        show: false,
        userUuid: "",
      }
    };
  },
  mounted() {
    this.initData();
  },
  methods: {
    moreOperation({ row, op }) {
      if (op == "enable") {
        this.enable(row);
      } else if (op == "del") {
        this.del(row);
      }
    },
    initData() {
      this.tableLoading = true;
      this.$axios.get("/user/search", {
        params: {
          page: this.tablePage,
          username: this.searchWords
        },
      }).then(res => {
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
        this.tableLoading = false;
      }).catch(() => {
        this.tableLoading = false;
      });
    },
    submitAdd() {
      this.$refs["form"].validate((valid) => {
        if (valid) {
          this.addLoading = true;
          const url = this.form.uuid ? '/user/edit' : '/user/add'
          const params = {
            realname: this.form.realname,
            password: this.form.password,
            "org.uuid": this.form.orgUuid,
            expiredTime: this.form.expiredTime,
            roleIds: this.form.roleIds.join(",")
          }
          if (this.form.uuid) {
            params.uuid = this.form.uuid
          } else {
            params.username = this.form.username
          }
          this.$axios.post(url,this.$qs.stringify(params)).then(res => {
            if (res.data.code == 0) {
              this.$message.success("操作成功");
              this.showAddDialog = false;
              this.initData();
            } else {
              this.$message.error(res.data.msg ? res.data.msg : "操作失败");
            }
            this.addLoading = false;
          }).catch(() => {
            this.$message.error("操作失败");
            this.addLoading = false;
          });
        }
      });
    },
    enable(row) {
      let url = !row.disabled ? "/user/disable" : "/user/enable";
      this.tableLoading = true;
      this.$axios.post(url, this.$qs.stringify({
        uuid: row.uuid,
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
      this.$confirm(`确认删除?`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      }).then(() => {
        this.tableLoading = true;
        this.$axios.post("/user/delete", this.$qs.stringify({
          uuid: row.uuid,
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
      });
    },
    search() {
      this.tablePage = 1
      this.initData()
    },
    remoteSearchOrg(query) {
      if (typeof query === 'object') {
        query = ''
      }
      this.remoteLoading = true
      this.$axios.get("/org/top50",{
        params:{
          name: query
        }
      }).then(res=>{
        this.remoteLoading = false
        if(res.data.code == 0) {
          this.orgs = res.data.data
        }
      })
    },
    remoteSearchRole() {
      this.$axios.get('/role').then(res => {
        if (res.data.code == 0) {
          this.roles = res.data.data;
        }
      });
    },
    closeAddDialog() {
      this.form.uuid = ''
      this.form.realname = ''
      this.form.username = ''
      this.form.password = ''
      this.form.orgUuid = ''
      this.form.roleIds = []
      this.form.expiredTime = ''
      this.roles = []
    },
    editUser(row) {
      this.form.uuid = row.uuid
      this.form.realname = row.realname
      this.form.orgUuid = row.org.uuid
      this.form.expiredTime = row.expiredTime
      this.orgs = [{uuid: row.org.uuid, name: row.org.name}]
      this.$axios.get('/user/role',{
        params:{
          uuid: row.uuid
        }
      }).then(res => {
        if (res.data.code == 0 && this.form.uuid) {
          if (this.roles.length == 0) {
            this.roles = res.data.data
          }
          this.form.roleIds = res.data.data.map(e => e.id)
        }
      });
      this.showAddDialog = true
    },
    showUserProfile(row) {
      this.userProfile.show = true
      this.userProfile.userUuid = row.uuid
    }
  },
};
</script>

<style scoped>
</style>
