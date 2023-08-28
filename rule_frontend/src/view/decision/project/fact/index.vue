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
          <!-- <dropdown split-button @click="exportExcel">
            <i class="el-icon-download"></i>
            {{exportLoading ? '导出中...' : '导出文件'}}
            <dropdown-menu slot="dropdown" v-if="$hasPerm('project.asset:edit')">
              <dropdown-item>
                <el-upload
                  action
                  :on-change="importExcel"
                  :auto-upload="false"
                  :show-file-list="false"
                  accept=".xlsx">
                  <i class="iconfont iconVersionupdaterule-copy"></i> 导入文件
                </el-upload>
              </dropdown-item>
            </dropdown-menu>
          </dropdown> -->
          <refer-btn :assets="assets"></refer-btn>
          <assets-editor-dialog
            :assets="assets"
            @refresh="(assets) => {$emit('refresh', assets)}">
          </assets-editor-dialog>
          <!-- <delete-btn :assets="assets" @delete="uuid => {$emit('delete', uuid)}"></delete-btn> -->
        </div>
      </el-col>
    </el-row>

    <!-- <el-form inline>
      <el-form-item label="模型英文名称">
        <el-input
          v-model.trim="enName"
          placeholder="请输入模型英文名称"
          size="small"
          :maxlength="maxLength"
          :disabled="readonly"
          ref="enNameInput">
        </el-input>
      </el-form-item>
      <el-form-item label="传递方向">
        <el-select
          v-model="direction"
          size="small"
          :disabled="readonly"
          placeholder="请选择">
          <el-option value="IN_OUT" label="传入传出"></el-option>
          <el-option value="IN" label="仅传入"></el-option>
          <el-option value="OUT" label="仅传出"></el-option>
          <el-option value="NONE" label="不传入不传出"></el-option>
        </el-select>
      </el-form-item>
    </el-form> -->

    <el-row>
      <el-col>
        <el-table
          :data="fields"
          border
          class="tb-edit"
          highlight-current-row
          ref="table"
          @cell-click="cellClick">
          <el-table-column label="英文名称" align="center">
            <template slot-scope="scope">
              <el-input
                v-if="!readonly && scope.row.id == clickRowId"
                v-model.trim="scope.row.name"
                placeholder="请输入"
                @keyup.enter.native="addField"
                :maxlength="maxLength"
                :ref="'input_name_' + scope.row.id">
              </el-input>
              <span>{{scope.row.name}}</span>
            </template>
          </el-table-column>
          <el-table-column label="中文名称" align="center">
            <template slot-scope="scope">
              <el-input
                v-if="!readonly && scope.row.id == clickRowId"
                v-model.trim="scope.row.label"
                placeholder="请输入"
                @keyup.enter.native="addField"
                :maxlength="maxLength"
                :ref="'input_label_' + scope.row.id">
              </el-input>
              <span>{{scope.row.label}}</span>
            </template>
          </el-table-column>
          <el-table-column label="数据类型" align="center">
            <template slot-scope="scope">
              <el-select
                v-if="!readonly && scope.row.id == clickRowId"
                v-model="scope.row.type"
                placeholder="请选择"
                :ref="'input_type_' + scope.row.id"
                @change="type => changeType(scope.row)">
                <el-option
                  v-for="type in $utils.fieldTypeArr"
                  :label="type != 'Derive' ? type : '衍生字段'"
                  :value="type"
                  :key="type">
                </el-option>
              </el-select>
              <span>{{scope.row.type != 'Derive' ? scope.row.type : '衍生字段'}}</span>
            </template>
          </el-table-column>
          <el-table-column label="子类型" align="center">
            <template slot-scope="scope">
              <template v-if="!readonly">
                <template v-if="scope.row.id == clickRowId">
                  <el-select
                    v-model="scope.row.subType"
                    placeholder="请选择"
                    filterable
                    :ref="'input_subType_' + scope.row.id"
                    v-if="$utils.isCollection(scope.row.type)">
                    <el-option-group v-for="group in subTypes" :key="group.label" :label="group.label">
                      <el-option v-for="type in group.options" :key="type.value" :value="type.value" :label="type.label"></el-option>
                    </el-option-group>
                  </el-select>
                  <el-select
                    v-model="scope.row.subType"
                    placeholder="请选择"
                    filterable
                    :ref="'input_subType_' + scope.row.id"
                    v-else-if="$utils.isObject(scope.row.type)">
                    <el-option v-for="type in subTypes[0].options" :key="type.value" :value="type.value" :label="type.label"></el-option>
                  </el-select>
                </template>
                <expression v-model="scope.row.deriveData" v-if="scope.row.type == 'Derive'"></expression>
                <span>{{getSubTypeName(scope.row.subType)}}</span>
              </template>
              <template v-else>
                <expression v-model="scope.row.deriveData" v-if="scope.row.type == 'Derive'" readonly></expression>
                <span v-else>{{getSubTypeName(scope.row.subType)}}</span>
              </template>
            </template>
          </el-table-column>
          <el-table-column label="传递方向" align="center">
            <template slot-scope="scope">
              <el-select
                v-if="!readonly && scope.row.id == clickRowId"
                v-model="scope.row.direction"
                placeholder="请选择"
                :ref="'input_direction_' + scope.row.id">
                <el-option value="IN_OUT" label="传入传出"></el-option>
                <el-option value="IN" label="仅传入"></el-option>
                <el-option value="OUT" label="仅传出"></el-option>
                <el-option value="NONE" label="不传入不传出"></el-option>
              </el-select>
              <span v-if="scope.row.direction == 'IN'">仅传入</span>
              <span v-else-if="scope.row.direction == 'OUT'">仅传出</span>
              <span v-else-if="scope.row.direction == 'NONE'">不传入不传出</span>
              <span v-else>传入传出</span>
            </template>
          </el-table-column>
          <el-table-column label="是否必输" align="center">
            <template slot-scope="scope" v-if="scope.row.direction == 'IN' || scope.row.direction == 'IN_OUT'">
              <el-select
                v-if="!readonly && scope.row.id == clickRowId"
                v-model="scope.row.required"
                placeholder="请选择"
                :ref="'input_required_' + scope.row.id">
                <el-option value="true" label="是"></el-option>
                <el-option value="false" label="否"></el-option>
              </el-select>
              <span>{{scope.row.required == 'true' ? '是' : '否'}}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" align="center" v-if="!readonly">
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
import expression from "../components/expression";

const javaKeywords = [
  'abstract', 'assert', 'boolean', 'break', 'byte', 'case', 'catch', 'char', 'class', 'const', 'continue',
  'default', 'do', 'double', 'else', 'enum', 'extends', 'final', 'finally', 'float', 'for', 'goto', 'if',
  'implements', 'import', 'instanceof', 'int', 'interface', 'long', 'native', 'new', 'package', 'private',
  'protected', 'public', 'return', 'strictfp', 'short', 'static', 'super', 'switch', 'synchronized', 'this',
  'throw', 'throws', 'transient', 'try', 'void', 'volatile', 'while', 'Boolean', 'Character', 'Double', 'Float',
  'Integer', 'Long', 'Short', 'BigDecimal', 'String', 'Object', 'List', 'Set', 'Map', 'Date'
]

export default {
  name: "index",
  components: {
    "delete-btn": deleteBtn,
    "refer-btn": referBtn,
    expression
  },
  data() {
    return {
      fields: [],
      showImportErrorDialog: false,
      importErrors: [],
      maxLength: 50,
      enName: '',
      historyEnName: '',
      clickRowId: '',
      direction: 'IN_OUT',
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
  computed: {
    subTypes: function() {
      const groups = [{label: '对象类型', options: []}, {label: '基本类型', options: []}]
      this.$store.state.facts.forEach(e => {
        if (e.uuid != this.assets.uuid) {
          groups[0].options.push({
            value: e.uuid,
            label: e.name
          })
        }
      });
      this.$utils.fieldBaseTypeArr.forEach(e => {
        groups[1].options.push({
          value: e,
          label: e
        })
      });
      return groups
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
          if (this.$utils.isDerive(e.type)) {
            if (e.deriveData) {
              e.deriveData = JSON.parse(e.deriveData)
            } else {
              e.deriveData = {}
            }
          }
          if (!e.required) {
            this.$set(e, 'required', 'false')
          }
        });
        if (json.enName) {
          this.enName = json.enName
          this.historyEnName = json.enName
        }
      }
    },
    addField() {
      const id = this.genFieldId()
      let field = {
        id: id,
        name: '',
        label: "",
        type: "String",
        subType: "",
        direction: "IN_OUT",
        deriveData: {},
        required: 'false'
      };
      this.fields.push(field);
      this.clickRowId = id
      this.$nextTick(() => {
        this.$refs["table"].setCurrentRow(field);
        this.$nextTick(() => {
          this.$refs["input_name_" + field.id].focus();
        })
      })
    },
    delField(scope) {
      this.$confirm('确定删除该字段吗 ?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      .then(() => {
        this.fields.splice(scope.$index, 1);
      })
      .catch(() => {})
    },
    validate() {
      // if (!this.enName) {
      //   this.$message.warning("请填写模型英文名称");
      //   this.$refs.enNameInput.focus()
      //   return false;
      // }
      // if (!/^[A-Za-z][A-Za-z0-9]*$/.test(this.enName)) {
      //   this.$message.warning("模型英文名称格式不正确，支持字母、数字，并且以字母开头");
      //   this.$refs.enNameInput.focus()
      //   return false;
      // }
      if (javaKeywords.indexOf(this.enName) != -1) {
        this.$message.warning("模型英文名称不能使用JAVA关键字");
        this.$refs.enNameInput.focus()
        return false;
      }
      if (this.fields.length < 1) {
        this.$message.warning("请添加字段");
        return false;
      }

      let valid = true;
      this.fields.some(e => {
        if (!e.name) {
          this.$message.warning("请填写英文名称");
          this.$refs["table"].setCurrentRow(e);
          this.$nextTick(() => {
            this.$refs["input_name_" + e.id].focus();
          })
          valid = false;
          return true;
        } else if (!/^[A-Za-z]\w*$/.test(e.name)) {
          this.$message.warning('字段英文名称格式不正确，支持字母、数字、下划线，并且以字母开头')
          this.$refs['table'].setCurrentRow(e)
          this.$nextTick(() => {
            this.$refs["input_name_" + e.id].focus();
          })
          valid = false
          return true
        } else if (javaKeywords.indexOf(e.name) != -1) {
          this.$message.warning("字段英文名称不能使用JAVA关键字");
          this.$refs['table'].setCurrentRow(e)
          this.$nextTick(() => {
            this.$refs["input_name_" + e.id].focus();
          })
          valid = false
          return true
        }

        if (!e.label) {
          this.$message.warning("请填写字段中文名称");
          this.$refs["table"].setCurrentRow(e);
          this.$nextTick(() => {
            this.$refs["input_label_" + e.id].focus();
          })
          valid = false;
          return true;
        } else if (e.label.indexOf('.') != -1) {
          this.$message.warning("字段中文名称不能包含字符[.]");
          this.$refs["table"].setCurrentRow(e);
          this.$nextTick(() => {
            this.$refs["input_label_" + e.id].focus();
          })
          valid = false;
          return true;
        }

        if ((this.$utils.isList(e.type) || this.$utils.isObject(e.type)) && this.$utils.isBlank(e.subType)) {
          this.$message.warning("请选择字段子类型");
          this.$refs["table"].setCurrentRow(e);
          valid = false;
          return true;
        }
      });

      if (!valid) return false;

      let fieldNames = [];
      let fieldLabels = [];
      this.fields.forEach(e => {
        fieldNames.push(e.name);
        fieldLabels.push(e.label);
      });
      fieldNames.sort();
      fieldLabels.sort();
      for (let i = 0; i < fieldNames.length - 1; i++) {
        if (
          fieldNames[i] == fieldNames[i + 1] ||
          fieldLabels[i] == fieldLabels[i + 1]
        ) {
          this.$message.warning("请去除重复字段");
          return false;
        }
      }
      return true;
    },
    cellClick(row, column, cell, event) {
      if (this.readonly) return

      this.clickRowId = row.id
      this.$nextTick(function() {
        if (column.label == "英文名称") {
          this.$refs["input_name_" + row.id].focus();
        } else if (column.label == "中文名称") {
          this.$refs["input_label_" + row.id].focus();
        } else if (column.label == "数据类型") {
          this.$refs["input_type_" + row.id].toggleMenu();
        } else if (column.label == "数据类型") {
          this.$refs["input_type_" + row.id].toggleMenu();
        } else if (column.label == "子类型") {
          this.$refs["input_subType_" + row.id] && this.$refs["input_subType_" + row.id].toggleMenu();
        } else if (column.label == "传递方向") {
          this.$refs["input_direction_" + row.id].toggleMenu();
        } else if (column.label == "是否必输") {
          this.$refs["input_required_" + row.id] && this.$refs["input_required_" + row.id].toggleMenu();
        }
      });
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

          const [nameIndex, labelIndex, typeIndex, subTypeIndex, directionIndex, requiredIndex] = indexArr;
          const fields = [];
          const errors = [];
          lines.forEach((cells, i) => {
            if (i > 0) {
              let name = this.cleanText(cells[nameIndex]);
              let label = this.cleanText(cells[labelIndex]);
              let type = this.cleanText(cells[typeIndex]);
              let subType = this.cleanText(cells[subTypeIndex]);
              let direction = this.cleanText(cells[directionIndex]);
              let required = this.cleanText(cells[requiredIndex]);
              let errMsgs = [];

              if (!name) {
                errMsgs.push('英文名称不能为空');
              } else if (!/^[A-Za-z]\w*$/.test(name)) {
                errMsgs.push('英文名称格式不正确，支持字母、数字、下划线，并且以字母开头');
              } else if (name.length > this.maxLength) {
                errMsgs.push(`英文名称不能超过${this.maxLength}个字符`);
              } else if (javaKeywords.indexOf(name) != -1) {
                errMsgs.push('英文名称不能使用JAVA关键字');
              }

              if (!label) {
                errMsgs.push('中文名称不能为空');
              } else if (label.length > this.maxLength) {
                errMsgs.push(`中文名称不能超过${this.maxLength}个字符`);
              } else if (label.indexOf('.') != -1) {
                errMsgs.push('中文名称不能包含字符[.]');
              }

              if (!type) {
                errMsgs.push('数据类型不能为空');
              } else if (type === '衍生字段') {
                type = 'Derive';
              } else if (this.$utils.fieldTypeArr.indexOf(type) == -1) {
                errMsgs.push('不支持的数据类型');
              }

              if (subType && this.$utils.fieldTypeArr.indexOf(subType) == -1) {
                errMsgs.push('不支持的子类型');
              }

              if (!direction) {
                direction = 'IN_OUT';
              } else {
                direction = this.selectDirection(direction);
                if (!direction) {
                  errMsgs.push('不支持的传递方向');
                }
              }

              if (!required) {
                required = 'false';
              } else {
                if (required != '是' && required != '否') {
                  errMsgs.push('是否必输只能选择是否');
                } else if (required == '是') {
                  required = 'true';
                } else {
                  required = 'false';
                }
              }

              if (errMsgs.length == 0) {
                fields.push({
                  id: this.genFieldId(),
                  name,
                  label,
                  type,
                  subType,
                  direction,
                  deriveData: {},
                  required
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
      let indexArr = ['英文名称', '中文名称', '数据类型', '子类型', '传递方向', '是否必输']
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
    selectDirection(val) {
      if (val == '仅传入') {
        return 'IN'
      } else if (val == '仅传出') {
        return 'OUT'
      } else if (val == '不传入不传出') {
        return 'NONE'
      } else if (val == '传入传出') {
        return 'IN_OUT'
      }
    },
    exportExcel() {
      if (this.exportLoading) return;

      let showTips = false, types = ['List', 'Map', 'Derive'];
      this.fields.some(f => {
        if (types.indexOf(f.type) != -1) {
          showTips = true;
          return true;
        }
      });

      if (showTips) {
        this.$confirm('子类型是复杂类型或衍生变量将不会导出', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'info'
        }).then(() => {
          this.doExport();
        })
      } else {
        this.doExport();
      }
    },
    doExport() {
      const excelHeader = ['英文名称', '中文名称', '数据类型', '子类型', '传递方向', '是否必输']
      const filterVal = ['name', 'label', 'type', 'subType', 'direction', 'required']
      const data = this.formatJson(filterVal, this.fields);
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
          if (k == 'direction') {
            if (f[k] == 'IN') {
              return '仅传入'
            } else if (f[k] == 'OUT') {
              return '仅传出'
            } else if (f[k] == 'NONE') {
              return '不传入不传出'
            } else {
              return '传入传出'
            }
          } else if (k == 'subType') {
            if (this.$utils.fieldBaseTypeArr.indexOf(f[k]) != -1) {
              return f[k]
            } else {
              return ''
            }
          } else if (k == 'required') {
            if (f[k] == 'true') {
              return '是'
            } else {
              return '否'
            }
          } else {
            if (k == 'type' && f[k] == 'Derive') {
              return '衍生字段'
            } else {
              return f[k]
            }
          }
        })
      })
    },
    moveUp(scope) {
      if (scope.$index > 0) {
        this.swapField(scope.$index, scope.$index - 1);
      } else if (scope.$index <= 0) {
          this.$message({
          message: '顶部的字段不可上移',
          type: 'warning'
        });
        }
    },
    moveDown(scope) {
      if (scope.$index < this.fields.length - 1) {
        this.swapField(scope.$index, scope.$index + 1);
      } else if (scope.$index >= this.fields.length - 1) {
          this.$message({
          message: '底部的字段不可下移',
          type: 'warning'
        });
      }
    },
    swapField(index1, index2) {
      let fields = [];
      this.fields.forEach(e => {
        fields.push(e);
      });
      const tmp = fields[index1];
      fields[index1] = fields[index2];
      fields[index2] = tmp;
      this.fields = fields;
    },
    getSubTypeName(subType) {
      let subTypeName
      this.subTypes.some(group => {
        group.options.some(option => {
          if (subType == option.value) {
            subTypeName = option.label
            return true
          }
        })
        if (subTypeName) return true
      })
      return subTypeName
    },
    changeType(row) {
      row.subType = "";
      //清空表达式
      if (this.$utils.isDerive(row.type)) {
        row.deriveData = {};
      } else {
        row.deriveData = "";
      }
    },
    saveCallback(callback) {
      this.$refs["table"].setCurrentRow();
      if (this.validate()) {
        if (this.historyEnName && this.historyEnName != this.enName) {
          this.$confirm('模型英文名称被修改，将会导致决策服务生效后报文字段变更，是否继续？', '提示', {
            confirmButtonText: '是',
            cancelButtonText: '否',
            type: 'warning'
          }).then(() => {
            this.doSaveCallback(callback)
            this.historyEnName = this.enName
          })
        } else {
          this.doSaveCallback(callback)
        }
      }
    },
    doSaveCallback(callback) {
      const fields = this.$utils.copy(this.fields)
      fields.forEach(e => {
        if (this.$utils.isDerive(e.type)) {
          e.deriveData = JSON.stringify(e.deriveData)
        } else {
          e.deriveData = ''
        }
      })
      let dataString = JSON.stringify({ fields: fields, enName: this.enName, direction: this.direction });
      dataString = dataString.replace(/\\r/g, "").replace(/\\n/g, "");
      callback(dataString, () => {
        this.$store.dispatch("loadBoms", {
          projectUuid: this.assets.projectUuid,
          ignoreFunc: true,
          ignoreModel: true,
          ignoreThirdApi: true
        });
      });
    },
    genFieldId() {
      return this.$utils.randomCode(16);
    }
  }
};
</script>

<style scoped>
.tb-edit .el-input,
.tb-edit .el-select,
.tb-edit .el-dropdown {
  display: none;
}

.tb-edit .current-row .el-input,
.tb-edit .current-row .el-select,
.tb-edit .current-row .el-select + .el-dropdown {
  display: inline-block;
}

.tb-edit .current-row .el-input + span,
.tb-edit .current-row .el-select + span,
.tb-edit .current-row .el-dropdown + span {
  display: none;
}
</style>
