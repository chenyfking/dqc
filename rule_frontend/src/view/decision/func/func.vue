<template>
  <div>
    <el-card v-loading="loading" element-loading-text="数据加载中...">
      <el-container>
        <el-aside width="210px" style="background: #fff;">
          <!-- <el-input
            v-model.trim="searchWords"
            size="mini"
            suffix-icon="el-icon-search"
            @change="search"
            clearable
            class="search"
            placeholder="搜索">
          </el-input> -->
          <el-button type="text" @click="showAddDialog = true" v-if="$hasPerm('function:add')">添加函数</el-button>
          <el-tree :data="treeData" @node-click="clickTreeNode" highlight-current ref="tree" node-key="key"></el-tree>
        </el-aside>
        <el-main>
          <div v-if="activeFunction.uuid" class="detail">
            <el-row>
              <el-col :span="3">
                <label>创建时间</label>
              </el-col>
              <el-col :span="6">
                <span>{{$moment(activeFunction.createTime).format("YYYY-MM-DD HH:mm")}}</span>
              </el-col>
              <el-col :span="3">
                <label>创建人</label>
              </el-col>
              <el-col :span="6">
                <span>{{activeFunction.creator.realname}}</span>
              </el-col>
              <el-col :span="3" :offset="3">
                <template v-if="activeFunction.jarName">
                  <i class="el-icon-loading icon-btn" v-if="downloadLoading"></i>
                  <i class="el-icon-download icon-btn rule-btn"
                     @click="downloadFunc"
                     v-else></i>
                </template>
                <template v-else-if="$hasPerm('function:edit')">
                  <i class="el-icon-loading icon-btn" v-if="editLoading"></i>
                  <i class="el-icon-edit icon-btn rule-btn"
                     @click="editFunc"
                     v-else>
                  </i>
                </template>
                <el-popconfirm
                  v-if="$hasPerm('function:delete')"
                  title="确定删除当前函数？"
                  @onConfirm="deleteFunc(activeFunction.uuid)">
                  <i class="el-icon-delete icon-btn rule-btn" slot="reference"></i>
                </el-popconfirm>
              </el-col>
            </el-row>
            <el-row v-if="activeFunction.jarName">
              <el-col :span="3" v-if="activeFunction.jarName">
                <label>函数包</label>
              </el-col>
              <el-col :span="6">
                <span>{{activeFunction.jarName}}</span>
              </el-col>
            </el-row>
          </div>

          <el-table :data="activeFunction.methods" ref="table">
            <el-table-column type="expand">
              <template slot-scope="props">
                <el-table :data="props.row.params" border :style="{width: expandTableWidth()}">
                  <el-table-column type="index"></el-table-column>
                  <el-table-column label="参数名称" prop="name"></el-table-column>
                  <el-table-column label="参数类型" prop="type"></el-table-column>
                </el-table>
              </template>
            </el-table-column>
            <el-table-column label="函数中文名称" prop="name"></el-table-column>
            <el-table-column label="函数英文名称" prop="declare"></el-table-column>
            <el-table-column label="函数返回类型" prop="returnType"></el-table-column>
          </el-table>
        </el-main>
      </el-container>
    </el-card>

    <el-dialog
      ref="dialog"
      title="编写函数集"
      :visible.sync="showAddDialog"
      fullscreen
      custom-class="func-dialog"
      @opened="openDialog"
      @closed="closeAddDialog">
      <el-tabs v-model="activeTab" v-loading="editorLoading" element-loading-text="初始化编辑框...">
        <el-tab-pane label="编写" name="src">
          <el-form :model="srcForm" :rules="srcRules" ref="srcForm">
            <el-form-item prop="src">
              <div ref="editor" name="src"></div>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="上传" name="jar" v-if="!srcForm.uuid">
          <el-form :model="jarForm" :rules="jarRules" ref="jarForm">
            <el-form-item label="函数包" prop="file">
              <el-upload
                action
                accept=".jar"
                ref="upload"
                :limit="1"
                :on-change="changeFile"
                :on-remove="() => jarForm.file = null"
                :auto-upload="false"
                :show-file-list="true">
                <el-button type="primary" icon="el-icon-files">选择文件</el-button>
              </el-upload>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
      <span slot="footer">
        <el-button @click="showAddDialog = false">取 消</el-button>
        <el-button type="primary" @click="submit" v-loading="addLoading">{{addLoading ? "提交中..." : "确 定"}}</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
  export default {
    name: "func",
    data() {
      return {
        loading: false,
        functions: [],
        filterFunctions: [],
        treeData: [],
        activeFunction: {},
        searchWords: '',
        showAddDialog: false,
        activeTab: 'src',
        srcForm: {
          uuid: '',
          src: ''
        },
        addLoading: false,
        jarForm: {
          file: null,
        },
        srcRules: {
          src: [
            { required: true, message: "请编写函数", trigger: "blur" }
          ]
        },
        jarRules: {
          file: [
            { required: true, message: "请上传函数包", trigger: "change" }
          ]
        },
        editLoading: false,
        downloadLoading: false,
        editor: null,
        editorLoading: false
      }
    },
    mounted() {
      this.loadFunctions();
    },
    methods: {
      loadFunctions(nodeKey) {
        this.functions = []
        this.loading = true
        this.$axios.get('/function/all').then(res => {
          if (res.data.code == 0 && res.data.data.length > 0) {
            this.functions = res.data.data
            this.search()
            this.activeNode(nodeKey)
          }
          this.loading = false
        }).catch(() => {
          this.loading = false
        })
      },
      clickTreeNode(data, node, component) {
        this.filterFunctions.some(f => {
          if (f.name == data.key || f.uuid == data.key) {
            this.activeFunction = f
            return true
          }
        })
      },
      submit(ctrlS) {
        if (this.activeTab == 'src') {
          this.submitSrc(ctrlS)
        } else {
          this.submitJar()
        }
      },
      submitSrc(ctrlS) {
        this.srcForm.src = this.editor.getValue().trim()
        this.$refs.srcForm.validate(valid => {
          if (valid) {
            this.addLoading = true
            const url = this.srcForm.uuid ? '/function/edit' : '/function/add'
            this.$axios.post(url, this.$qs.stringify(this.srcForm)).then(res => {
              if (res.data.code == 0) {
                this.$message.success("提交成功")
                if (ctrlS !== true) {
                  this.showAddDialog = false
                }
                this.loadFunctions(res.data.data)
              } else {
                if (res.data.msg) {
                  const msg = res.data.msg.replace(/\n/g, '<br>').replace(/ /g, '&ensp;')
                  this.$message.error({
                    message: msg,
                    duration: 5000,
                    dangerouslyUseHTMLString: true,
                    showClose: true
                  })
                } else {
                  this.$message.error("提交失败")
                }
              }
              this.addLoading = false
            }).catch(() => {
              this.$message.error("提交失败")
              this.addLoading = false
            })
          }
        })
      },
      search() {
        const items = this.$utils.copy(this.functions)
        if (this.searchWords) {
          // 根据关键词模糊搜索方法中文名称或英文名称
          items.forEach(f => {
            f.methods = f.methods.filter(m => {
              m.name.indexOf(this.searchWords) != -1 || m.declare.indexOf(this.searchWords) != -1
            })
          })
        }

        this.filterFunctions = []
        items.forEach(f => {
          if (f.methods && f.methods.length > 0) {
            this.filterFunctions.push(f)
          }
        })

        this.treeData = []
        this.filterFunctions.forEach(f => {
          this.treeData.push({
            uuid: f.uuid,
            label: f.name,
            key: f.uuid ? f.uuid : f.name // 树节点key，有uuid用uuid，没有用name
          })
        })
      },
      changeFile(file) {
        this.jarForm.file = file
        this.$refs.jarForm.validate()
      },
      submitJar() {
        this.$refs.jarForm.validate(valid => {
          if (valid) {
            const params = new FormData()
            params.append("file", this.jarForm.file.raw)
            this.addLoading = true
            this.$axios.post('/function/upload', params, {
              headers: { "Content-Type": "multipart/form-data" }
            }).then(res => {
              if (res.data.code == 0) {
                this.$message.success("上传成功")
                this.showAddDialog = false
                this.loadFunctions()
              } else {
                this.$message.error(res.data.msg ? res.data.msg : "上传失败")
              }
              this.addLoading = false
            }).catch(() => {
              this.$message.error("上传失败")
              this.addLoading = false
            })
          }
        })
      },
      closeAddDialog() {
        if (this.jarForm.file) {
          this.$refs.upload.clearFiles();
        }
        this.srcForm.uuid = ''
        this.$refs.srcForm && this.$refs.srcForm.resetFields()
        this.$refs.jarForm && this.$refs.jarForm.resetFields()
      },
      deleteFunc(uuid) {
        this.loading = true
        this.$axios.post('/function/delete', this.$qs.stringify({
          uuid: uuid
        })).then(res => {
          this.loading = false
          if (res.data.code == 0) {
            this.$message.success("删除成功")
            this.loadFunctions()
          } else {
            this.$message.error(res.data.msg ? res.data.msg : "删除失败")
          }
        }).catch(() => {
          this.$message.error("删除失败")
          this.loading = false
        })
      },
      editFunc() {
        this.editLoading = true
        this.$axios.get('/function/view', {
          params: {
            uuid: this.activeFunction.uuid
          }
        }).then(res => {
          if (res.data.code == 0) {
            this.showAddDialog = true
            this.$nextTick(() => {
              this.srcForm.uuid = this.activeFunction.uuid
              this.srcForm.src = res.data.data
            })
          } else {
            this.$message.error(res.data.msg ? res.data.msg : "操作失败")
          }
          this.editLoading = false
        }).catch(() => {
          this.$message.error("操作失败")
          this.editLoading = false
        })
      },
      downloadFunc() {
        const that = this
        this.downloadLoading = true
        this.$axios({
          method: "get",
          url: "/function/download?uuid=" + this.activeFunction.uuid,
          responseType: "blob"
        }).then(res => {
          this.downloadLoading = false
          if (res.data.type == "application/json") {
            const reader = new FileReader();
            reader.onload = function() {
              const json = JSON.parse(reader.result);
              if (json.code == 401) {
                localStorage.removeItem('hasLogin')
                const from = that.$router.currentRoute.fullPath
                that.$router.push('/login?from=' + from)
              } else {
                that.$message.error(json.msg ? json.msg : "下载失败");
              }
            };
            reader.readAsText(res.data);
          } else {
            let url = window.URL.createObjectURL(new Blob([res.data]));
            let a = document.createElement("a");
            a.style.display = "none";
            a.href = url;
            a.target = "_blank";
            a.setAttribute("download", that.activeFunction.jarName);
            document.body.appendChild(a);
            a.click();
          }
        }).catch(() => {
          this.$message.error("下载失败")
          this.downloadLoading = false
        })
      },
      activeNode(nodeKey) {
        if (this.treeData.length > 0) {
          this.$nextTick(() => {
            if (nodeKey) {
              this.$refs.tree.setCurrentKey(nodeKey)
              this.clickTreeNode({key: nodeKey})
            } else {
              this.$refs.tree.setCurrentKey(this.treeData[0].key)
              this.clickTreeNode(this.treeData[0])
            }
          })
        } else {
          this.activeFunction = {}
        }
      },
      openDialog() {
        this.initEditor().then(() => {
          this.editor.setValue(this.srcForm.src);
        }).catch(e => {
          this.$message.error('初始化编辑框失败');
        });
      },
      async initEditor() {
        this.$refs.editor.style.height = this.$refs.dialog.$el.offsetHeight - 200 + 'px';
        if (this.editor == null) {
          const self = this;
          try {
            this.editorLoading = true;
            const monaco = await import('monaco-editor/esm/vs/editor/editor.api');
            const editor = monaco.editor.create(this.$refs.editor, {
              language: "java"
            });

            editor.addCommand(monaco.KeyMod.CtrlCmd | monaco.KeyCode.KEY_S, function() {
              self.submit(true);
            });
            this.editorLoading = false;

            this.editor = editor;
          } catch (e) {
            this.editorLoading = false;
            throw new Error(e);
          }
        } else {
          this.editor.layout();
        }
      },
      expandTableWidth() {
        // 解决safari扩展表格死循环问题
        const isSafari = /Safari/.test(navigator.userAgent) && !/Chrome/.test(navigator.userAgent);
        if (isSafari) {
          return this.$refs.table.$el.offsetWidth - 100 + 'px';
        }
        return '100%';
      }
    },
    beforeDestroy() {
      this.editor != null && this.editor.dispose();
    }
  }
</script>

<style scoped>
  .detail {
    margin-left: 15px;
    margin-bottom: 10px;
  }

  .detail label {
    color: #909399;
  }
</style>
