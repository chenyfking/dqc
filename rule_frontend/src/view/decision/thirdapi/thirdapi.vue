<template>
  <div>
    <el-card v-loading="loading" element-loading-text="数据加载中...">
      <el-container>
        <el-aside width="210px" style="background: #fff;">
          <el-button type="text" @click="showAddDialog = true" v-if="$hasPerm('thirdapi:add')">添加外部接口</el-button>
          <el-tree :data="treeData" @node-click="clickTreeNode" highlight-current ref="tree" node-key="label"></el-tree>
        </el-aside>
        <el-main>
          <div v-if="activeApi.uuid" class="detail">
            <el-row>
              <el-col :span="3">
                <label>请求地址</label>
              </el-col>
              <el-col :span="18">
                <span>{{activeApi.url}}</span>
              </el-col>
              <el-col :span="3">
                <i class="el-icon-edit icon-btn rule-btn"
                   @click="editApi(activeApi.uuid)"
                   v-if="$hasPerm('thirdapi:edit')"></i>
                <el-popconfirm
                  title="确定删除当前接口？"
                  @onConfirm="deleteApi(activeApi.uuid)"
                  v-if="$hasPerm('thirdapi:delete')">
                  <i class="el-icon-delete icon-btn rule-btn" slot="reference"></i>
                </el-popconfirm>
              </el-col>
            </el-row>
            <el-row>
              <el-col :span="3">
                <label>参数类型</label>
              </el-col>
              <el-col :span="6">
                <span>{{activeApi.requestContextType == 'JSON' ? 'JSON' : 'KEY-VALUE'}}</span>
              </el-col>
              <el-col :span="3">
                <label>结果PATH</label>
              </el-col>
              <el-col :span="6">
                <span>{{activeApi.responseJsonPath}}</span>
              </el-col>
            </el-row>
            <el-row>
              <el-col :span="3">
                <label>请求方法</label>
              </el-col>
              <el-col :span="6">
                <span>{{activeApi.method}}</span>
              </el-col>
              <el-col :span="3">
                <label>更新时间</label>
              </el-col>
              <el-col :span="6">
                <span>{{$moment(activeApi.updateTime).format("YYYY-MM-DD HH:mm")}}</span>
              </el-col>
            </el-row>
            <el-row>
              <el-col :span="3">
                <label>创建时间</label>
              </el-col>
              <el-col :span="6">
                <span>{{$moment(activeApi.createTime).format("YYYY-MM-DD HH:mm")}}</span>
              </el-col>
              <el-col :span="3">
                <label>创建人</label>
              </el-col>
              <el-col :span="6">
                <span>{{activeApi.creator.realname}}</span>
              </el-col>
            </el-row>
            <label>请求头部</label>
            <el-table :data="activeApi.headers" border style="margin-top: 10px;">
              <el-table-column prop="name" label="名称" align="center"></el-table-column>
              <el-table-column prop="value" label="值" align="center"></el-table-column>
            </el-table>
          </div>
        </el-main>
      </el-container>
    </el-card>

    <el-dialog
      :title="form.uuid ? '编辑外部接口' : '添加外部接口'"
      :top="$dialogTop"
      :visible.sync="showAddDialog"
      @opened="openAddDialog"
      @closed="closeAddDialog">
      <el-form ref="form" :model="form" :rules="rules" label-width="80px" @submit.native.prevent>
        <el-form-item label="接口名称" prop="name">
          <el-input
            v-model.trim="form.name"
            :maxlength="30"
            show-word-limit
            clearable
            ref="name"
            @keyup.enter.native="submit"
            placeholder="请填写接口名称">
          </el-input>
        </el-form-item>
        <el-form-item label="请求地址" prop="url">
          <el-input
            v-model.trim="form.url"
            :maxlength="250"
            show-word-limit
            clearable
            @keyup.enter.native="submit"
            placeholder="请填写请求地址">
          </el-input>
        </el-form-item>
        <el-form-item label="请求方法" prop="method">
          <el-select v-model="form.method" placeholder="请选择请求方法">
            <el-option label="GET" value="GET"></el-option>
            <el-option label="POST" value="POST"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="参数类型" prop="requestContentType" v-if="form.method == 'POST'">
          <el-select v-model="form.requestContentType" placeholder="请选择参数类型">
            <el-option label="KEY-VALUE" value="X_WWW_FORM_URLENCODED"></el-option>
            <el-option label="JSON" value="JSON"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="结果PATH" prop="responseJsonPath">
          <el-input
            v-model.trim="form.responseJsonPath"
            :maxlength="30"
            show-word-limit
            clearable
            @keyup.enter.native="submit"
            placeholder="请填写结果PATH">
          </el-input>
        </el-form-item>
        <el-form-item label="请求头部">
          <el-row v-for="(header, i) in form.headers" :key="i">
            <el-col :span="1">{{i + 1}}.</el-col>
            <el-col :span="9">
              <el-input
                v-model.trim="header.name"
                clearable
                placeholder="请填写头部名称">
              </el-input>
            </el-col>
            <el-col :span="9" :offset="1">
              <el-input
                v-model.trim="header.value"
                clearable
                placeholder="请填写头部值">
              </el-input>
            </el-col>
            <el-col :span="4">
              <i class="el-icon-plus icon-btn" @click="addHeader(i)"></i>
              <i class="el-icon-delete icon-btn" @click="delHeader(i)"></i>
            </el-col>
          </el-row>
          <el-button type="text" @click="addHeader">添加头部</el-button>
        </el-form-item>
      </el-form>

      <span slot="footer">
        <el-button @click="showAddDialog = false">取 消</el-button>
        <el-button type="primary" @click="submit" v-loading="addLoading">{{addLoading ? "提交中..." : "确 定"}}</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
  export default {
    name: "thirdapi",
    data() {
      return {
        loading: false,
        showAddDialog: false,
        addLoading: false,
        form: {
          name: '',
          url: '',
          method: 'POST',
          headers: [],
          requestContentType: 'X_WWW_FORM_URLENCODED',
          responseJsonPath: '',
          headers: [{}]
        },
        rules: {
          name: [
            { required: true, message: '请填写接口名称', trigger: 'blur' },
            { max: 30, message: '长度不能超过30个字符', trigger: 'blur' }
          ],
          url: [
            { required: true, message: '请填写请求地址', trigger: 'blur' },
            { max: 250, message: '长度不能超过250个字符', trigger: 'blur' }
          ],
          method: [
            { required: true, message: '请选择请求方法', trigger: 'change' }
          ],
          requestContentType: [
            { required: true, message: '请选择参数类型', trigger: 'change' }
          ],
          responseJsonPath: [
            { max: 30, message: '长度不能超过30个字符', trigger: 'blur' }
          ]
        },
        treeData: [],
        apis: [],
        activeApi: {}
      }
    },
    mounted() {
      this.loadApis()
    },
    methods: {
      loadApis() {
        this.loading = true
        this.treeData = []
        this.$axios.get('/thirdapi/all').then(res => {
          if (res.data.code == 0 && res.data.data.length > 0) {
            this.apis = res.data.data
            this.apis.forEach(e => {
              this.treeData.push({
                uuid: e.uuid,
                label: e.name
              })
            })

            this.$nextTick(function() {
              this.$refs.tree.setCurrentKey(this.treeData[0].label)
              this.clickTreeNode(this.treeData[0])
            })
          } else {
              this.activeApi = {}
          }
          this.loading = false
        }).catch(() => {
          this.loading = false
        })
      },
      openAddDialog() {
        this.$refs.name.focus()
      },
      closeAddDialog() {
        this.$refs.form.resetFields()
        this.form.uuid = ''
        this.form.headers = []
      },
      addHeader(i) {
        this.form.headers.splice(i + 1, 0, {})
      },
      delHeader(i) {
        this.form.headers.splice(i, 1)
        if (this.form.headers.length == 0) {
          this.form.headers.push({})
        }
      },
      submit() {
        this.$refs.form.validate(valid => {
          if (valid) {
            const params = {
              name: this.form.name,
              url: this.form.url,
              method: this.form.method,
              requestContentType: this.form.requestContentType,
              responseJsonPath: this.form.responseJsonPath
            }
            if (this.form.uuid) {
              params.uuid = this.form.uuid
            }
            const headers = this.form.headers.filter(h =>
              this.$utils.isNotBlank(h.name) && this.$utils.isNotBlank(h.value)
            )
            headers.forEach((e, i) => {
              params[`headers[${i}].name`] = e.name
              params[`headers[${i}].value`] = e.value
            })
            const url = params.uuid ? '/thirdapi/edit' : '/thirdapi/add'

            this.addLoading = true
            this.$axios.post(url, this.$qs.stringify(params)).then(res => {
              if (res.data.code == 0) {
                this.$message.success(params.uuid ? '编辑成功' : '添加成功')
                this.showAddDialog = false
                this.loadApis()
              } else {
                this.$message.error(res.data.msg ? res.data.msg : '操作失败')
              }
              this.addLoading = false
            }).catch(() => {
              this.addLoading = false
            })
          }
        })
      },
      clickTreeNode(data) {
        this.apis.some(e => {
          if (e.name == data.label) {
            this.activeApi = e
            return true
          }
        })
      },
      deleteApi(uuid) {
        this.loading = true
        this.$axios.post('/thirdapi/delete', this.$qs.stringify({
          uuid: uuid
        })).then(res => {
          this.loading = false
          if (res.data.code == 0) {
            this.$message.success("删除成功")
            this.loadApis()
          } else {
            this.$message.error(res.data.msg ? res.data.msg : "删除失败")
          }
        }).catch(() => {
          this.$message.error("删除失败")
          this.loading = false
        })
      },
      editApi(uuid) {
        this.apis.some(e => {
          if (e.uuid == uuid) {
            this.form = this.$utils.copy(e)
            this.showAddDialog = true
            return true
          }
        })
      }
    }
  }
</script>

<style scoped>
  .tb-edit .el-input {
    display: none;
  }

  .tb-edit .current-row .el-input {
    display: inline-block;
  }

  .tb-edit .current-row .el-input + span {
    display: none;
  }

  .detail label {
    color: #909399;
  }
</style>
