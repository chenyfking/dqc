<template>
  <div>
    <el-row ref="toolbar" v-if="!readonly">
      <el-col class="toolbar">
        <div class="pin">
          <save-btn @callback="saveCallback" :assets="assets" @template="ruleTemplate" @refresh="refresh"></save-btn>
          <test-btn :assets="assets"></test-btn>
          <el-button icon="el-icon-plus" @click="addRow" v-if="$hasPerm('project.asset:edit')">添加行</el-button>
          <el-button v-if="tableData.rows.length > 10" icon="el-icon-place" @click="fixHeader = !fixHeader">{{fixHeader ? '取消固定表头' : '固定表头'}}</el-button>
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
          <import-btn :assets="assets" @importTpl="importTpl"></import-btn>
          <refer-btn :assets="assets"></refer-btn>
          <assets-validation :assets="assets"></assets-validation>
          <assets-editor-dialog :assets="assets" @refresh="refresh"></assets-editor-dialog>
          <delete-btn :assets="assets" @delete="uuid => {$emit('delete', uuid)}"></delete-btn>
        </div>
      </el-col>
    </el-row>

    <el-row>
      <el-col>
        <el-switch
          v-if="fireRules.length > 0"
          v-model="showFireRules"
          active-text="仅显示触发规则"
          @change="filterRules"
          style="margin-bottom: 10px;">
        </el-switch>
        <div class="el-table el-table--fit el-table--border el-table--enable-row-hover el-table--enable-row-transition ruletable-table"
             :class="{'el-table--scrollable-y': fixHeader}"
             :style="{width: '100%;', height: tableHeight}">
          <div class="el-table__header-wrapper" ref="tableHeaderWrapper">
            <table class="el-table__header" cellpadding="0" cellspacing="0" border="0" style="width: 100% !important;">
              <thead class="has-gutter">
                <tr>
                  <th v-for="header, i in tableData.headers" :key="'header_' + header._id" class="is-center is-leaf" :class="[{'action-column': header.type == 'ASSIGN'},header.cssStyle]">
                    <div class="cell" v-if="header.type == 'ATTR'">
                      <dropdown @command="command => selectAttr(command, i)" v-if="!readonly">
                        <span style="cursor: pointer; color: #909399;">{{getAttrLabel(i)}}</span>
                        <dropdown-menu slot="dropdown">
                          <dropdown-item v-for="attr in getAttrItems(i)" :key="attr.name" :command="attr.name">{{attr.label}}</dropdown-item>
                        </dropdown-menu>
                      </dropdown>
                      <span v-else>{{getAttrLabel(i)}}</span>
                      <dropdown style="float: right;" @command="command => handleHeader(command, i)" v-if="!readonly">
                        <i class="el-icon-arrow-down el-icon--right icon-btn-mini"></i>
                        <dropdown-menu slot="dropdown">
                          <dropdown-item icon="el-icon-arrow-left" command="addLeftAttr">添加属性列（前）</dropdown-item>
                          <dropdown-item icon="el-icon-arrow-right" command="addRightAttr">添加属性列（后）</dropdown-item>
                          <dropdown-item icon="el-icon-delete" command="deleteColumn" divided>删除当前属性列</dropdown-item>
                        </dropdown-menu>
                      </dropdown>
                    </div>
                    <div class="cell" v-else>
                      <fact v-model="header.fact" v-if="header.type !== 'ASSIGN'" :readonly="readonly"></fact>
                      <fact v-model="header.fact" scope="ASSIGN" v-else :readonly="readonly"></fact>
                      <dropdown style="float: right;" @command="command => handleHeader(command, i)" v-if="!readonly">
                        <i class="el-icon-arrow-down el-icon--right icon-btn-mini"></i>
                        <dropdown-menu slot="dropdown" v-if="header.type == 'CONDITION'">
                          <dropdown-item icon="el-icon-arrow-left" command="addLeftAttr">添加属性列（前）</dropdown-item>
                          <dropdown-item icon="el-icon-arrow-left" command="addLeftCondition">添加条件列（前）</dropdown-item>
                          <dropdown-item icon="el-icon-arrow-right" command="addRightCondition">添加条件列（后）</dropdown-item>
                          <dropdown-item icon="el-icon-delete" command="deleteColumn" divided v-if="showDeleteCondition">删除当前条件列</dropdown-item>
                        </dropdown-menu>
                        <dropdown-menu slot="dropdown" v-else>
                          <dropdown-item icon="el-icon-arrow-left" command="addLeftAssign">添加赋值列（前）</dropdown-item>
                          <dropdown-item icon="el-icon-arrow-right" command="addRightAssign">添加赋值列（后）</dropdown-item>
                          <dropdown-item icon="el-icon-delete" command="deleteColumn" divided v-if="showDeleteAction">删除当前动作列</dropdown-item>
                        </dropdown-menu>
                      </dropdown>
                    </div>
                  </th>
                  <th class="is-center is-leaf" width="150" v-if="$hasPerm('project.asset:edit') && !readonly">
                    <div class="cell">操作</div>
                  </th>
                  <th v-if="fixHeader" class="gutter" style="width: 17px;"></th>
                </tr>
              </thead>
            </table>
          </div>

          <div class="el-table__body-wrapper is-scrolling-none" :style="{height: tableBodyHeight}">
            <table class="el-table__body" cellpadding="0" cellspacing="0" border="0" style="width: 100% !important;">
              <tbody>
                <tr v-for="row, i in tableData.rows" :key="'row_' + row[0]._id" class="el-table__row">
                  <td v-for="cell, j in row"
                      :key="'cell_' + cell._id"
                      class="is-center"
                      :class="[{'action-column': tableData.headers[j].type == 'ASSIGN',},tableData.headers[j].cssStyle]"
                      v-contextmenu="contextmenuData(i, j)">
                    <div class="cell">
                      <template v-if="tableData.headers[j].type === 'ATTR'">
                        <template v-if="!readonly">
                          <el-input-number v-model="row[j].value"
                                           :controls="false"
                                           style="width: 100%;"
                                           v-if="tableData.headers[j].name === 'salience'"></el-input-number>
                          <el-date-picker v-model="row[j].value"
                                          v-else-if="tableData.headers[j].name === 'date-effective'"
                                          type="datetime"
                                          value-format="yyyy-MM-dd HH:mm:ss"
                                          style="width: 100%;"
                                          placeholder="请选择生效日期"></el-date-picker>
                          <el-date-picker v-model="row[j].value"
                                          v-else-if="tableData.headers[j].name === 'date-expires'"
                                          type="datetime"
                                          value-format="yyyy-MM-dd HH:mm:ss"
                                          style="width: 100%;"
                                          :picker-options="expiredTimePickerOptions"
                                          placeholder="请选择失效日期"></el-date-picker>
                          <el-input v-model="row[j].value"
                                    style="width: 100%;"
                                    v-else-if="tableData.headers[j].name === 'activation-group'"></el-input>
                          <el-switch v-model="row[j].value"
                                     v-else-if="tableData.headers[j].name === 'enabled'"
                                     active-color="#13ce66"></el-switch>
                        </template>
                        <template v-else>
                          <span>{{row[j].value}}</span>
                        </template>
                      </template>
                      <template v-else-if="tableData.headers[j].fact">
                        <template v-if="!readonly">
                          <el-popover trigger="click" v-if="tableData.headers[j].type == 'CONDITION'" @show="$refs[`lhs_${i}_${j}`][0].draw()">
                            <lhs v-model="row[j]" :left-type="getHeaderType(tableData.headers[j])" :ref="'lhs_' + i + '_' + j"></lhs>
                            <el-button slot="reference" type="text" style="white-space: normal; line-height: 1.5;">
                              <span v-html="$lhs.getText(row[j])"></span>
                            </el-button>
                          </el-popover>
                          <expression v-model="row[j].right" :input-type="getHeaderType(tableData.headers[j])" v-else></expression>
                          <i class="el-icon-delete icon-btn-mini" @click="$set(tableData.rows[i], j, {})"></i>
                        </template>
                        <template v-else>
                          <span v-html="$lhs.getText(row[j])" v-if="tableData.headers[j].type == 'CONDITION'" style="white-space: normal; line-height: 1.5;"></span>
                          <expression v-model="row[j].right" :input-type="getHeaderType(tableData.headers[j])" v-else readonly></expression>
                        </template>
                      </template>
                      <template v-else>-</template>
                    </div>
                  </td>
                  <td class="is-center" width="150" v-if="$hasPerm('project.asset:edit') && !readonly">
                    <div class="cell">
                      <i class="el-icon-document-copy icon-btn" @click="duplicateRow(i)"></i>
                      <i class="el-icon-top icon-btn" @click="moveUp(i)"></i>
                      <i class="el-icon-bottom icon-btn" @click="moveDown(i)"></i>
                      <i class="el-icon-delete icon-btn" @click="$delete(tableData.rows, i)"></i>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
            <div v-if="tableData.rows.length == 0" class="el-table__empty-block" style="height: 100%; width: 100%;">
              <span class="el-table__empty-text">暂无数据</span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
  import lhs from '../components/lhs'
  import deleteBtn from '../assets/delete-btn'
  import testBtn from '../assets/test-btn'
  import referBtn from "../assets/assets-refer-btn";
  import importBtn from "../assets/assets-import-template";
  import assetsValidation from "../assets/assets-validation-rules";

  let _tableData

  export default {
    components: {
      lhs,
      'delete-btn': deleteBtn,
      'test-btn': testBtn,
      'refer-btn': referBtn,
      'import-btn': importBtn,
      'assets-validation': assetsValidation,
    },
    name: "index",
    props: {
      assets: Object,
      readonly: {
        type: Boolean,
        default: false
      },
      fireRules: {
        type: Array,
        default: function() {
          return []
        }
      }
    },
    data() {
      return {
        showAttrDialog: false,
        saveLoading: false,
        saveVersionLoading: false,
        showNewVersionDialog: false,
        execlSetUrl:'/execl/importruletable',
        execlDownUrl:'/execl/downruletabletemplate',
        tableData: {
          headers: [
            {type: 'CONDITION', _id: this.$utils.randomCode(16)},
            {type: 'ASSIGN', _id: this.$utils.randomCode(16)}
          ],
          rows: []
        },
        showVersionDialog:false,
        nowVersion: {
          headers: [
            {type: 'CONDITION'},
            {type: 'ASSIGN'}
          ],
          rows: []
        },
        expiredTimePickerOptions: {
          disabledDate: time => {
            return time.getTime() < new Date().getTime()
          },
          firstDayOfWeek: 1
        },
        targetVersion: {
          headers: [
            {type: 'CONDITION'},
            {type: 'ASSIGN'}
          ],
          rows: []
        },
        fixHeader: false,
        _copyCondition: null,
        _copyAction: null,
        showFireRules: true,
        exportLoading: false
      }
    },
    computed: {
      showDeleteCondition() {
        let count = 0
        this.tableData.headers.forEach(e => {
          if (e.type == 'CONDITION') {
            count++
          }
        })
        return count > 1
      },
      showDeleteAction() {
        let count = 0
        this.tableData.headers.forEach(e => {
          if (e.type == 'ASSIGN') {
            count++
          }
        })
        return count > 1
      },
      tableHeight() {
        if (!this.fixHeader) return '100%'
        const mainHeight = document.querySelector('.el-main').offsetHeight - 40
        const pageHeaderHeight = document.querySelector('.el-page-header').offsetHeight + 20
        const tabsHeaderHeight = document.querySelector('.el-tabs__header').offsetHeight
        const toolbarHeight = this.$refs.toolbar.$el.offsetHeight + 5
        const tableHeight = mainHeight - pageHeaderHeight - tabsHeaderHeight - toolbarHeight - 20
        return tableHeight + 'px'
      },
      tableBodyHeight() {
        if (!this.fixHeader) return '100%'
        const tableHeaderHeight = this.$refs.tableHeaderWrapper.offsetHeight
        return parseInt(this.tableHeight) - tableHeaderHeight + 'px'
      }
    },
    mounted() {
      this.renderTableData()
    },
    methods: {
      renderTableData(){
        if (!this.$utils.isBlank(this.assets.content)) {
          _tableData = JSON.parse(this.assets.content)
          _tableData.headers.forEach(header => {
            if (!header._id) {
              header._id = this.$utils.randomCode(16)
            }
          })
          _tableData.rows.forEach(row => {
            row.forEach(cell => {
              if (!cell._id) {
                cell._id = this.$utils.randomCode(16)
              }
            })
          })
          this.filterRules()
        }
      },
      handleHeader(command, i) {
        const headers = JSON.parse(JSON.stringify(this.tableData.headers))
        const rows = JSON.parse(JSON.stringify(this.tableData.rows))
        if (command == 'addLeftAttr') {
          headers.splice(i, 0, {type: 'ATTR', _id: this.$utils.randomCode(16), name: ''})
          rows.forEach(row => row.splice(i, 0, {_id: this.$utils.randomCode(16), value: ''}))
        } else if (command == 'addRightAttr') {
          headers.splice(i + 1, 0, {type: 'ATTR', _id: this.$utils.randomCode(16), name: ''})
          rows.forEach(row => row.splice(i + 1, 0, {_id: this.$utils.randomCode(16), value: ''}))
        } else if (command == 'addLeftCondition') {
          headers.splice(i, 0, {type: 'CONDITION', _id: this.$utils.randomCode(16)})
          rows.forEach(row => row.splice(i, 0, {_id: this.$utils.randomCode(16)}))
        } else if (command == 'addRightCondition') {
          headers.splice(i + 1, 0, {type: 'CONDITION', _id: this.$utils.randomCode(16)})
          rows.forEach(row => row.splice(i + 1, 0, {_id: this.$utils.randomCode(16)}))
        } else if (command == 'addLeftAssign') {
          headers.splice(i, 0, {type: 'ASSIGN', _id: this.$utils.randomCode(16)})
          rows.forEach(row => row.splice(i, 0, {_id: this.$utils.randomCode(16)}))
        } else if (command == 'addRightAssign') {
          headers.splice(i + 1, 0, {type: 'ASSIGN', _id: this.$utils.randomCode(16)})
          rows.forEach(row => row.splice(i + 1, 0, {_id: this.$utils.randomCode(16)}))
        } else if (command == 'deleteColumn') {
          headers.splice(i, 1)
          rows.forEach(row => row.splice(i, 1))
        }
        this.tableData.headers = headers
        this.tableData.rows = rows
      },
      addRow() {
        let row = []
        this.tableData.headers.forEach(e => {
          row.push({_id: this.$utils.randomCode(16)})
        })
        this.tableData.rows.push(row)
      },
      importTpl(tplAssets){
        this.assets.content = tplAssets
        this.renderTableData()
      },
      duplicateRow(i) {
        const row = JSON.parse(JSON.stringify(this.tableData.rows[i]))
        row.forEach(cell => {
          cell._id = this.$utils.randomCode(16)
        })
        this.tableData.rows.splice(i + 1, 0, row)
      },
      moveUp(index) {
        if (index > 0) {
          this.swapRow(index, index - 1)
        }
      },
      moveDown(index) {
        if (index < this.tableData.rows.length - 1) {
          this.swapRow(index, index + 1)
        }
      },
      swapRow(index1, index2) {
        let rows = []
        this.tableData.rows.forEach(e => {
          rows.push(e)
        })
        const tmp = rows[index1]
        rows[index1] = rows[index2]
        rows[index2] = tmp
        this.tableData.rows = rows
      },
      save() {
        const tableData = JSON.parse(JSON.stringify(this.tableData))
        this.$utils.clearTmpKey(tableData)
        this.saveLoading = true;
        this.$axios.post("/assets/savecontent", this.$qs.stringify({
          uuid: this.assets.uuid,
          content: JSON.stringify(tableData)
        })).then((res) => {
          this.saveLoading = false;
          if (res.data.code == 0) {
            this.$message.success('保存成功')
          } else {
            this.$message.error(res.data.msg ? res.data.msg : '保存失败')
          }
        }).catch(() => {
          this.$message.error('保存失败')
          this.saveLoading = false;
        })
      },
      //execl表上传，触发渲染。
      execlUpSuccess(data){
        this.assets.content = data
        this.renderTableData()
      },
      getHeaderType(header) {
        return this.$store.getters.getExpressionType({type: 'FACT', fact: header.fact})
      },
      rowKey(row) {
        if (!row[0]._id) row[0]._id = this.$utils.randomCode(16)
        return row[0]._id
      },
      contextmenuData(i, j) {
        const header = this.tableData.headers[j]
        if (!header.fact) return null
        const cell = this.tableData.rows[i][j]
        const items = []
        const _this = this
        if (header.type == 'CONDITION') {
          items.push({
            text: '复制条件',
            command: cell,
            click: cell => _this._copyCondition = cell
          })
          items.push({
            text: '粘贴条件',
            command: {i, j},
            click: function ({i, j}) {
              if (!_this._copyCondition) return
              let copyCell = _this.$utils.copy(_this._copyCondition)
              copyCell._id = _this.$utils.randomCode(16)
              _this.$set(_this.tableData.rows[i], j, copyCell)
            }
          })
        } else {
          items.push({
            text: '复制动作',
            command: cell,
            click: cell => _this._copyAction = cell
          })
          items.push({
            text: '粘贴动作',
            command: {i, j},
            click: function ({i, j}) {
              if (!_this._copyAction) return
              let copyCell = _this.$utils.copy(_this._copyAction)
              copyCell._id = _this.$utils.randomCode(16)
              _this.$set(_this.tableData.rows[i], j, copyCell)
            }
          })
        }
        return { items }
      },
      saveCallback(callback) {
        const tableData = JSON.parse(JSON.stringify(this.tableData))
        this.$utils.clearTmpKey(tableData)
        callback(JSON.stringify(tableData))
      },
      ruleTemplate(){
        this.$emit('addTemplate')
      },
      selectAttr(command, i) {
        if (this.tableData.headers[i].name !== command) {
          this.tableData.headers[i].name = command
          this.tableData.rows.forEach(row => {
            row[i].value = ''
          })
        }
      },
      getAttrLabel(i) {
        const header = this.tableData.headers[i]
        const label = this.$attr.getLabel(header.name)
        return label || '请选择'
      },
      exportExcel() {
        if (this.exportLoading) return;

        const excelHeader = [];  // 设置Excel的表格第一行的标题
        this.tableData.headers.forEach(header => {
          if (header.type == 'ATTR') {
            const label = this.$attr.getLabel(header.name)
            if (label) {
              excelHeader.push('属性列:' + label)
            }
          } else if (header.type == 'CONDITION') {
            if (header.fact) {
              const text = this.$store.getters.getFactText(header.fact.id, header.fact.fieldId)
              if (text) {
                excelHeader.push('条件列:' + text)
              }
            }
          } else if (header.type == 'ASSIGN') {
            if (header.fact) {
              const text = this.$store.getters.getFactText(header.fact.id, header.fact.fieldId)
              if (text) {
                excelHeader.push('赋值列:' + text)
              }
            }
          }
        });

        const that = this;
        that.exportLoading = true;
        this.$axios({
          method: "get",
          url: "/assets/writeexcel",
          data: [excelHeader],
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
            const headers = this.parseHeaders(lines[0]);
            if (headers.length <= 0) {
              this.$message.error('导入失败，请检查文件是否正确');
              return;
            }

            const rows = [];
            lines.forEach((line, i) => {
              if (i > 0) {
                const cells = this.parseRow(line, headers)
                rows.push(cells)
              }
            });

            this.tableData.headers = headers;
            this.tableData.rows = rows;
            this.$message.success('导入成功');
          } else {
            this.$message.error(res.data.msg ? res.data.msg : "导入失败");
          }
        }).catch(e => {
          this.$message.error("导入失败");
          console.error(e);
        });
      },
      parseHeaders(header) {
        const headers = []
        header.forEach(e => {
          if (e.startsWith('属性列:')) {
            const label = e.substring(4)
            const name = this.$attr.getName(label)
            if (name) {
              headers.push({
                _id: this.$utils.randomCode(16),
                type: 'ATTR',
                name: name
              })
            }
          } else if (e.startsWith('条件列:') || e.startsWith('赋值列:')) {
            const text = e.substring(4)
            const factExpr = this.$store.getters.getFactExpressionByText(text)
            if (factExpr) {
              headers.push({
                _id: this.$utils.randomCode(16),
                type: e.startsWith('条件列:') ? 'CONDITION' : 'ASSIGN',
                fact: factExpr
              })
            }
          }
        })
        return headers
      },
      parseRow(line, headers) {
        const cells = []
        headers.forEach((h, i) => {
          const cell = {
            _id: this.$utils.randomCode(16)
          };
          cells.push(cell);

          const v = line[i] ? line[i].trim() : '';

          if (h.type === 'ATTR') {
            try {
              if (h.name == 'date-effective' || h.name == 'date-expires') {
                const value = this.transformValue('Date', v);
                if (value) cell.value = value
              } else if (h.name == 'enabled') {
                const value = this.transformValue('Boolean', v)
                if (value) cell.value = value
              } else if (h.name == 'salience') {
                const value = this.transformValue('Integer', v)
                if (value) cell.value = value
              } else {
                cell.value = v
              }
            } catch (exception) {
              console.error(exception)
            }
          } else if (h.type === 'CONDITION') {
            const type = this.getHeaderType(h);
            const value = this.transformValue(type, v);
            if (value) {
              cell.constraint = {
                conjunction: 'AND',
                constraints: [
                  {
                    op: 'EQ',
                    right: {
                      type: 'INPUT',
                      input: {
                        type: type,
                        value: value
                      }
                    }
                  }
                ]
              }
            }
          } else if (h.type === 'ASSIGN') {
            const type = this.getHeaderType(h);
            const value = this.transformValue(type, v);
            if (value) {
              cell.right = {
                type: 'INPUT',
                input: {
                  type: type,
                  value: value
                }
              }
            }
          }
        })
        return cells
      },
      transformValue(type, value) {
        try {
          if (type == 'Integer' || type == 'Long') {
            return parseInt(value)
          } else if (type == 'Double' || type == 'BigDecimal') {
            return parseFloat(value)
          } else if (type == 'Boolean') {
            value = value.toLowerCase()
            if (value === 'true') {
              return true
            } else if (value === 'false') {
              return false
            }
          } else if (type == 'Date') {
            const date = new Date(value)
            const times = date.getTime()
            if (times) {
              return this.$moment(times).format('YYYY-MM-DD HH:mm:ss')
            }
          } else {
            return value
          }
        } catch(e) {
          console.error(e)
          return null
        }
      },
      refresh(assets) {
        if (assets.changeVersion === true) {
          const tableData = JSON.parse(assets.content)
          tableData.headers.some(header => {
            if (!header._id) {
              header._id = this.$utils.randomCode(16)
            }
          })
          tableData.rows.forEach(row => {
            row.forEach(cell => {
              if (!cell._id) {
                cell._id = this.$utils.randomCode(16)
              }
            })
          })
          this.tableData = tableData
        } else {
          this.$emit('refresh',assets)
        }
      },
      getAttrItems(i) {
        const items = []
        this.$attr.items.forEach(attr => {
          let exists = false
          this.tableData.headers.some((header, j) => {
            if (header.type == 'ATTR' && i !== j && header.name == attr.name) {
              // 排除其它属性列已经有了的属性
              exists = true
              return true
            }
          })
          if (!exists) {
            items.push(attr)
          }
        })
        return items
      },
      filterRules() {
        if (this.fireRules.length == 0 || !this.showFireRules) {
          this.tableData.headers = _tableData.headers
          this.tableData.rows = _tableData.rows
          return
        }

        const rows = []
        this.fireRules.forEach(rule => {
          const ruleName = this.assets.versionNo
            ? rule.substring(`决策表-${this.assets.name}_V${this.assets.versionNo}-`.length)
            : rule.substring(`决策表-${this.assets.name}-`.length)
          const rowNum = parseInt(ruleName.substring(1, ruleName.length - 1))
          if (rowNum) {
            _tableData.rows.forEach((r, i) => {
              if (rowNum == i + 1) {
                rows.push(r)
              }
            })
          }
        })
        this.tableData.headers = _tableData.headers
        this.tableData.rows = rows
      }
    }
  }
</script>

<style scoped>
  .action-column {
    background-color: oldlace;
  }
</style>
