<template>
  <div>
    <el-row v-if="!readonly">
      <el-col class="toolbar">
        <div class="pin">
          <save-btn @callback="saveCallback" :assets="assets" :new-version="false"></save-btn>
          <el-button
            icon="el-icon-plus"
            @click="addField"
            v-if="$hasPerm('project.asset:edit')">
            添加字段
          </el-button>
          <!-- <dropdown
            split-button
            @click="exportExcel">
            <i class="el-icon-download"></i>
              {{exportLoading ? '导出中...' : '导出文件'}}
            <dropdown-menu slot="dropdown" v-if="$hasPerm('project.asset:edit')">
              <dropdown-item>
                <el-upload action="" :on-change="importExcel" :auto-upload="false"
                     :show-file-list="false" accept=".xlsx">
                  <i class="iconfont iconVersionupdaterule-copy"></i> 导入文件
                </el-upload>
              </dropdown-item>
            </dropdown-menu>
          </dropdown> -->
          <refer-btn :assets="assets"></refer-btn>
          <assets-editor-dialog :assets="assets" @refresh="(assets) => {$emit('refresh', assets)}"></assets-editor-dialog>
          <!-- <delete-btn :assets="assets" @delete="uuid => {$emit('delete', uuid)}"></delete-btn> -->
        </div>
      </el-col>
    </el-row>

    <el-row>
      <el-col>
        <el-table :data="fields" class="tb-edit" border highlight-current-row
                  ref="table" @cell-click="cellClick">
          <el-table-column label="中文名称" align="center">
            <template slot-scope="scope">
              <el-input v-model.trim="scope.row.label" placeholder="请输入标题"
                        :ref="'input_label_' + scope.row.id" v-if="!readonly" :maxlength="maxLength"></el-input>
              <span>{{scope.row.label}}</span>
            </template>
          </el-table-column>
          <el-table-column label="数据类型" align="center">
            <template slot-scope="scope">
              <el-select
                v-if="!readonly"
                v-model="scope.row.type"
                @change="type => changeType(scope.row)"
                :ref="'input_type_' + scope.row.id"
                placeholder="请选择数据类型">
                <el-option value="String"></el-option>
                <el-option value="Integer"></el-option>
                <el-option value="Double"></el-option>
                <el-option value="Long"></el-option>
                <el-option value="BigDecimal"></el-option>
                <el-option value="Boolean"></el-option>
                <el-option value="Date"></el-option>
              </el-select>
              <span>{{scope.row.type}}</span>
            </template>
          </el-table-column>
          <el-table-column label="值" align="center">
            <template slot-scope="scope">
              <template v-if="!readonly">
                <el-input-number v-if="$utils.isNumber(scope.row.type)"
                         v-model.trim="scope.row.value"
                         :controls="false" :ref="'input_value_' + scope.row.id"></el-input-number>
                <el-select v-else-if="$utils.isBoolean(scope.row.type)"
                           v-model="scope.row.value"
                           placeholder="请选择">
                  <el-option value="true" label="是"></el-option>
                  <el-option value="false" label="否"></el-option>
                </el-select>
                <el-date-picker v-else-if="$utils.isDate(scope.row.type)"
                                v-model="scope.row.value"
                                type="datetime"
                                value-format="yyyy-MM-dd HH:mm:ss"
                                placeholder="请选择"
                                align="center">
                </el-date-picker>
                <el-input v-model.trim="scope.row.value" v-else
                          :ref="'input_value_' + scope.row.id"></el-input>
                <span v-if="scope.row.type == 'Boolean'">
                  {{!scope.row.value ? '' : (scope.row.value === 'true' ? '是' : '否')}}
                </span>
                <span v-else>{{scope.row.value}}</span>
              </template>
              <template v-else>
                <span v-if="scope.row.type == 'Boolean'">
                  {{!scope.row.value ? '' : (scope.row.value === 'true' ? '是' : '否')}}
                </span>
                <span v-else>{{scope.row.value}}</span>
              </template>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" align="center" v-if="!readonly">
            <template slot-scope="scope">
              <i class="el-icon-delete icon-btn" @click.stop="delField(scope)"></i>
              <i class="el-icon-top icon-btn" @click.stop="moveUp(scope)"></i>
              <i class="el-icon-bottom icon-btn" @click.stop="moveDown(scope)"></i>
            </template>
          </el-table-column>
        </el-table>
      </el-col>
    </el-row>

    <el-dialog title="错误信息" :top="$dialogTop"
               :visible.sync="showImportErrorDialog">
      <el-alert type="error" show-icon title="导入文件失败，错误信息如下："></el-alert>
      <el-table :data="importErrors">
        <el-table-column label="文件中的行号" prop="rowNumber"></el-table-column>
        <el-table-column label="错误描述" prop="msgs" show-overflow-tooltip></el-table-column>
      </el-table>
      <div slot="footer">
        <el-button @click="showImportErrorDialog = false" icon="el-icon-error">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
  import deleteBtn from "../assets/delete-btn";
  import referBtn from "../assets/assets-refer-btn";

  export default {
    name: "index",
    components: {
      'delete-btn': deleteBtn,
      'refer-btn': referBtn
    },
    data() {
      return {
        fields: [],
        showImportErrorDialog: false,
        importErrors: [],
        maxLength: 50,
        exportLoading: false
      };
    },
    props: {
      assets: Object,
      readonly: {
        type: Boolean,
        default: false
      }
    },
    mounted() {
      this.initData();
    },
    methods: {
      initData() {
        if (!this.assets.content) {
          this.fields = [];
        } else {
          let json = JSON.parse(this.assets.content);
          this.fields = json.fields;
          this.fields.forEach(e => {
            if (!e.id) {
              e.id = this.genFieldId()
            }
          });
        }
      },
      addField() {
        const id = this.genFieldId();
        let field = {
          id: id,
          label: "",
          value: '',
          type: "String"
        };
        this.fields.push(field);
        this.$nextTick(() => {
          this.$refs["table"].setCurrentRow(field);
          this.$nextTick(() => {
            this.$refs["input_label_" + field.id].focus();
          })
        })
      },
      delField(scope) {
        this.fields.splice(scope.$index, 1);
      },
      validate() {
        if (this.fields.length < 1) {
          this.$message.warning("请添加字段");
          return false;
        }

        let valid = true;
        this.fields.some(e => {
          if (!e.label) {
            this.$message.warning("请输入中文名称");
            this.$refs["table"].setCurrentRow(e);
            this.$nextTick(() => {
              this.$refs["input_label_" + e.id].focus();
            })
            valid = false;
            return true;
          } else if (e.label.indexOf('.') != -1) {
            this.$message.warning("中文名称不能包含字符[.]");
            this.$refs["table"].setCurrentRow(e);
            this.$nextTick(() => {
              this.$refs["input_label_" + e.id].focus();
            })
            valid = false;
            return true;
          }
          if (e.value == null || e.value === "") {
            this.$message.warning("请输入值");
            this.$refs["table"].setCurrentRow(e);
            this.$nextTick(() => {
              this.$refs["input_value_" + e.id].focus();
            })
            valid = false;
            return true;
          }
        });

        if (!valid) return false;

        let fieldNames = [];
        this.fields.forEach(e => {
          fieldNames.push(e.label);
        });
        fieldNames.sort();
        for (let i = 0; i < fieldNames.length - 1; i++) {
          if (fieldNames[i] == fieldNames[i + 1]) {
            this.$message.warning("请去除重复字段");
            return false;
          }
        }
        return true;
      },
      importExcel(file) {
        if (!this.$utils.checkUploadFile(file, ['xlsx', 'xls'])) return

        const params = new FormData();
        params.append("file", file.raw);
        this.$axios.post('/assets/readexcel', params, {
          headers: { "Content-Type": "multipart/form-data" }
        }).then(res => {
          if (res.data.code == 0) {
            if (!res.data.data.csv || res.data.data.csv.length <= 0) {
              this.$message.error('导入文件失败，请检查文件是否正确');
              return;
            }

            const lines = res.data.data.csv[0].lines;
            const indexArr = this.getHeaderIndex(lines[0]);
            if (typeof indexArr === 'string') {
              this.$message.error(`导入文件失败，缺少列：[${indexArr}]`);
              return;
            }

            const [labelIndex, valueIndex, typeIndex] = indexArr;
            const fields = [];
            const errors = [];
            lines.forEach((cells, i) => {
              if (i > 0) {
                let label = this.cleanText(cells[labelIndex]);
                let value = this.cleanText(cells[valueIndex]);
                let type = this.cleanText(cells[typeIndex]);
                let errMsgs = [];

                if (!label) {
                  errMsgs.push('中文名称不能为空');
                } else if (label.length > this.maxLength) {
                  errMsgs.push(`中文名称不能超过${this.maxLength}个字符`);
                } else if (label.indexOf('.') != -1) {
                  errMsgs.push('中文名称不能包含字符[.]');
                }

                if (!value) {
                  errMsgs.push('值不能为空');
                } else {
                  if (this.$utils.isNumber(type) && isNaN(Number(value))) {
                    errMsgs.push('值必须是数字');
                  } else if (this.$utils.isBoolean(type)) {
                    if (value != '是' && value != '否') {
                      errMsgs.push('值只能选择是否');
                    } else if (value == '是') {
                      value = 'true';
                    } else {
                      value = 'false';
                    }
                  } else if (this.$utils.isDate(type)) {
                    try {
                      const date = new Date(value);
                      if (date.toString() === 'Invalid Date') {
                        errMsgs.push('值的日期格式不正确');
                      } else {
                        value = this.$moment(date).format('YYYY-MM-DD HH:mm:ss');
                      }
                    } catch(e) {
                      errMsgs.push('值的日期格式不正确');
                    }
                  }
                }

                if (!type) {
                  errMsgs.push('数据类型不能为空');
                } else if (this.$utils.fieldTypeArr.indexOf(type) == -1) {
                  errMsgs.push('不支持的数据类型');
                }

                if (errMsgs.length == 0) {
                  fields.push({
                    id: this.genFieldId(),
                    label,
                    value,
                    type
                  });
                } else {
                  errors.push({rowNumber: i + 1, msgs: errMsgs.join("；")});
                }
              }
            })

            if (errors.length == 0) {
              this.$message.success('导入成功');
              this.fields = fields;
            } else {
              this.showImportErrorDialog = true;
              this.importErrors = errors;
            }
          } else {
            this.$message.error(res.data.msg ? res.data.msg : "导入失败");
          }
        }).catch(e => {
          this.$message.error("导入失败");
          console.error(e);
        });
      },
      getHeaderIndex(headers) {
        let indexArr = ['中文名称', '值', '数据类型']
        headers.forEach((e, i) => {
          const h = this.cleanText(e)
          const index = indexArr.indexOf(h)
          if (index != -1) {
            indexArr[index] = i
          }
        })
        let lackHeaders = indexArr.filter(e => typeof e === 'string')
        if (lackHeaders.length > 0) {
          return lackHeaders.join(',')
        }
        return indexArr
      },
      cleanText(text) {
        return text ? text.trim() : '';
      },
      exportExcel() {
        if (this.exportLoading) return;

        const excelHeader = ['中文名称', '值', '数据类型']
        const filterVal = ['label', 'value', 'type']
        const data = this.formatJson(filterVal, this.fields)
        data.unshift(excelHeader);

        const that = this;
        that.exportLoading = true;
        this.$axios({
          method: "get",
          url: "/assets/writeexcel",
          data,
          responseType: "blob",
        }).then(res => {
          that.exportLoading = false;
          if (res.data.type == "application/json") {
            const reader = new FileReader();
            reader.onload = function () {
              const json = JSON.parse(reader.result);
              if (json.code == 401) {
                localStorage.removeItem('hasLogin')
                const from = that.$router.currentRoute.fullPath
                that.$router.push('/login?from=' + from)
              } else {
                that.$message.error(json.msg ? json.msg : "导出失败");
              }
            };
            reader.readAsText(res.data);
          } else {
            let url = window.URL.createObjectURL(new Blob([res.data]));
            let a = document.createElement("a");
            a.style.display = "none";
            a.href = url;
            a.target = "_blank";
            a.setAttribute("download", this.assets.name + ".xlsx");
            document.body.appendChild(a);
            a.click();
          }
        }).catch(() => {
          that.exportLoading = false;
          that.$message.error("导出失败");
        });
      },
      formatJson(keys, fields) {
        return fields.map(f => {
          return keys.map(k => {
            if (k == 'value' && this.$utils.isBoolean(f.type)) {
              return f[k] === 'true' ? '是' : '否'
            }
            return f[k]
          })
        })
      },
      cellClick(row, column, cell, event) {
        if (this.readonly) return

        this.$nextTick(() => {
          if (column.label == "中文名称") {
            this.$refs["input_label_" + row.id].focus();
          } else if (column.label == "数据类型") {
            this.$refs["input_type_" + row.id].toggleMenu();
          } else if (column.label == "值") {
            this.$refs["input_value_" + row.id] &&
            this.$refs["input_value_" + row.id].focus();
          }
        })
      },
      moveUp(scope) {
        if (scope.$index > 0) {
          this.swapField(scope.$index, scope.$index - 1)
        } else if (scope.$index <= 0) {
          this.$message({
          message: '顶部的字段不可上移',
          type: 'warning'
        });
        }
      },
      moveDown(scope) {
        if (scope.$index < this.fields.length - 1) {
          this.swapField(scope.$index, scope.$index + 1)
        } else if (scope.$index >= this.fields.length - 1) {
          this.$message({
          message: '底部的字段不可下移',
          type: 'warning'
        });
        }
      },
      swapField(index1, index2) {
        let fields = []
        this.fields.forEach(e => {
          fields.push(e)
        })
        const tmp = fields[index1]
        fields[index1] = fields[index2]
        fields[index2] = tmp
        this.fields = fields
      },
      saveCallback(callback) {
        this.$refs["table"].setCurrentRow();
        if (this.validate()) {
          callback(JSON.stringify({
            fields: this.fields
          }), () => {
            this.$store.dispatch('loadBoms', {
              projectUuid: this.assets.projectUuid,
              ignoreFunc: true,
              ignoreModel: true,
              ignoreThirdApi: true
            })
          })
        }
      },
      genFieldId() {
        return this.$utils.randomCode(16)
      },
      changeType(row) {
        row.value = ''
      }
    }
  };
</script>

<style scoped>
  .tb-edit .el-input,
  .tb-edit .el-input-number,
  .tb-edit .el-select {
    display: none;
    width: 100%;
  }

  .tb-edit .current-row .el-input,
  .tb-edit .current-row .el-input-number,
  .tb-edit .current-row .el-select {
    display: block;
  }

  .tb-edit .current-row .el-input + span,
  .tb-edit .current-row .el-input-number + span,
  .tb-edit .current-row .el-select + span {
    display: none;
  }
</style>
