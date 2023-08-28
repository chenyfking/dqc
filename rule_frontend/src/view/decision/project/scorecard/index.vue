<template>
  <div>
    <el-row v-if="!readonly">
      <el-col class="toolbar">
        <div class="pin">
          <save-btn @callback="saveCallback" :assets="assets" @refresh="refresh" @template="scorecardTemplate"></save-btn>
          <test-btn :assets="assets"></test-btn>
          <el-button icon="el-icon-plus" @click="addRow" v-if="$hasPerm('project.asset:edit')">添加行</el-button>
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
        <el-button type="text" v-if="$hasPerm('project.asset:edit') && !readonly" @click="showAttrDialog = true">设置属性</el-button>
        <el-tag v-for="(attr, index) in scorecard.attrs"
                :key="attr.name"
                size="small"
                type="warning"
                :closable="!readonly"
                @close="scorecard.attrs.splice(index, 1)"
                style="margin-right: 10px;">
          {{attr.label}}： {{attr.text}}
        </el-tag>
        <el-form :inline="true" style="margin-top: 5px;">
          <el-form-item label="支持权重：">
            <el-switch v-model="scorecard.weight" :disabled="readonly"></el-switch>
          </el-form-item>
          <el-form-item label="评分计算方式：">
            <el-select size="mini" style="width: 100px;" v-model="scorecard.scoringType" placeholder="请选择" :disabled="readonly">
              <el-option label="求和" value="SUM"></el-option>
              <el-option label="求积" value="MUL"></el-option>
              <el-option label="模型评分转换" value="MODELTRANSFORM"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="初始评分：">
            <el-input-number size="mini" style="width: 100px;" v-model="scorecard.defaultScore" :controls="false" :disabled="readonly"></el-input-number>
          </el-form-item>
          <br>
          <el-form-item label="将评分赋给：">
            <fact input-type="Double" v-model="scorecard.assign" :readonly="readonly"></fact>
          </el-form-item>
          <el-form-item label="将原因码赋给：">
            <fact input-type="String" v-model="scorecard.reasonCodes" :readonly="readonly"></fact>
          </el-form-item>
          <el-form-item label="将原因信息赋给：">
            <fact input-type="String" v-model="scorecard.reasonMsgs" :readonly="readonly"></fact>
          </el-form-item>
        </el-form>
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
        <el-table :data="scorecard.rows" :row-key="rowKey" border :span-method="rowspan" class="scorecard-table">
          <el-table-column label="字段" align="center">
            <template slot-scope="scope">
              <template v-if="!readonly">
                <fact v-model="scope.row.fact" @change="(newVal, oldVal) => changeField(newVal, oldVal, scope.$index)"></fact>
                <i class="el-icon-delete icon-btn-mini" @click="deleteRowGroup(scope)"></i>
                <i class="el-icon-plus icon-btn-mini" @click="insertRow(scope)"></i>
              </template>
              <template v-else>
                <span v-html="getLeftText(scope.row.fact)" v-if="scope.row.fact"></span>
                <span v-else>请选择</span>
              </template>
              <div v-if="scorecard.weight">
                <label>权重：</label>
                <el-input-number v-model.trim="scope.row.weight" size="small" :controls="false" :min="0" :max="1" :disabled="readonly"></el-input-number>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="condition" label="条件" align="center">
            <template slot-scope="scope">
              <template v-if="!readonly">
                <el-popover trigger="click" v-if="scope.row.fact" @show="$refs[`lhs_${scope.$index}`].draw()">
                  <lhs v-model="scope.row.condition" :left-type="getLeftType(scope.row.fact)" :ref="'lhs_' + scope.$index"></lhs>
                  <el-button slot="reference" type="text" style="white-space: normal; line-height: 1.5; padding-top: 0; padding-bottom: 0px;">
                    <span v-html="$lhs.getText(scope.row.condition)"></span>
                  </el-button>
                </el-popover>
                <span v-else>-</span>
                <i class="el-icon-delete icon-btn-mini" @click="deleteRow(scope)"></i>
                <i class="el-icon-document-copy icon-btn-mini" @click="copyRow(scope)"></i>
              </template>
              <template v-else>
                <span v-html="$lhs.getText(scope.row.condition)" v-if="scope.row.fact"></span>
                <span v-else>-</span>
              </template>
            </template>
          </el-table-column>

          <el-table-column prop="score"
                           align="center"
                           :label="scorecard.scoreLabel != null && scorecard.scoreLabel.trim() != '' ? scorecard.scoreLabel : '分值'"
                           :render-header="renderScoreHeader">
            <template slot-scope="scope">
              <expression v-model="scope.row.score" input-type="Double" :readonly="readonly"></expression>
            </template>
          </el-table-column>

          <el-table-column label="原因码" align="center">
            <template slot-scope="scope">
              <el-popover @after-enter="$refs['reasonCode_' + scope.$index].focus()" v-if="!readonly">
                <el-input v-model="scope.row.reasonCode" :ref="'reasonCode_' + scope.$index"></el-input>
                <el-link :underline="false" slot="reference">
                  {{scope.row.reasonCode ? scope.row.reasonCode : '-'}}
                </el-link>
              </el-popover>
              <span v-else>{{scope.row.reasonCode}}</span>
            </template>
          </el-table-column>

          <el-table-column label="原因信息" align="center">
            <template slot-scope="scope">
              <el-popover @after-enter="$refs['reasonMsg_' + scope.$index].focus()" v-if="!readonly">
                <el-input v-model="scope.row.reasonMsg" :ref="'reasonMsg_' + scope.$index"></el-input>
                <el-link :underline="false" slot="reference">
                  {{scope.row.reasonMsg ? scope.row.reasonMsg : '-'}}
                </el-link>
              </el-popover>
              <span v-else>{{scope.row.reasonMsg}}</span>
            </template>
          </el-table-column>
        </el-table>
      </el-col>
    </el-row>

    <attr :show.sync="showAttrDialog" :attrs.sync="scorecard.attrs"></attr>
  </div>
</template>

<script>
  import lhs from '../components/lhs'
  import deleteBtn from '../assets/delete-btn'
  import testBtn from '../assets/test-btn'
  import referBtn from "../assets/assets-refer-btn";
  import importBtn from "../assets/assets-import-template";
  import assetsValidation from "../assets/assets-validation-rules";

  export default {
    name: "index",
    components: {
      lhs,
      'delete-btn': deleteBtn,
      'test-btn': testBtn,
      "refer-btn": referBtn,
      "import-btn": importBtn,
      "assets-validation": assetsValidation
    },
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
      const empty = {
        name: this.assets.name,
        attrs: [],
        weight: false,
        scoringType: "SUM",
        assign: {},
        scoreLabel: null,
        rows: [],
        defaultScore: 0,
        reasonCodes: {},
        reasonMsgs: {}
      }
      return {
        saveLoading: false,
        empty: empty,
        scorecard: JSON.parse(JSON.stringify(empty)),
        showAttrDialog: false,
        showVersionDialog: false,
        showFireRules: true
      }
    },
    mounted() {
      this.initData()
    },
    methods: {
      initData() {
        if (this.assets.content) {
          this.filterRules()
        } else {
          this.scorecard = this.$utils.copy(this.empty)
        }
      },
      importTpl(tplAssets){
        this.assets.content = tplAssets
        this.initData()
      },
      renderScoreHeader(h, {column, $index}) {
        if (!this.readonly) {
          return <span>{column.label}
            <el-popover>
            <el-input v-model={this.scorecard.scoreLabel}></el-input>
            <i class="el-icon-edit" style="cursor: pointer;" slot="reference"></i>
          </el-popover>
          </span>
        }

        return <span>{column.label}</span>
      },
      addRow() {
        this.scorecard.rows.push({})
      },
      getLeftText(fact) {
        return this.$store.getters.getFactText(fact.id, fact.fieldId)
      },
      rowspan({row, column, rowIndex, columnIndex}) {
        if (columnIndex === 0) {
          const rows = this.scorecard.rows
          const curRow = rows[rowIndex]

          if (!curRow.fact) return [1, 1]

          if (rowIndex > 0) {
            const prevRow = rows[rowIndex - 1]
            if (prevRow.fact && curRow.fact.id == prevRow.fact.id && curRow.fact.fieldId == prevRow.fact.fieldId) {
              return [0, 0]
            }
          }

          let rowspan = 1
          for (let i = rowIndex + 1; i < rows.length; i++) {
            const iRow = rows[i]
            if (iRow.fact && curRow.fact.id == iRow.fact.id && curRow.fact.fieldId == iRow.fact.fieldId) {
              rowspan++
            } else {
              break
            }
          }
          return [rowspan, 1]
        }
      },
      insertRow(scope) {
        let insertIndex
        const rows = this.scorecard.rows
        const curRow = scope.row

        if (!curRow.fact) {
          insertIndex = scope.$index
        } else {
          for (let i = scope.$index; i < rows.length; i++) {
            const iRow = rows[i]
            if (iRow.fact && curRow.fact.id == iRow.fact.id && curRow.fact.fieldId == iRow.fact.fieldId) {
              insertIndex = i
            } else {
              break
            }
          }
        }

        if (curRow.fact) {
          rows.splice(insertIndex + 1, 0, {
            fact: this.$utils.copy(curRow.fact)
          })
        } else {
          rows.splice(insertIndex + 1, 0, {})
        }
      },
      copyRow(scope) {
        let row = this.$utils.copy(scope.row)
        row._id = this.$utils.randomCode(10)
        this.scorecard.rows.splice(scope.$index + 1, 0, row)
      },
      deleteRow(scope) {
        this.scorecard.rows.splice(scope.$index, 1)
      },
      deleteRowGroup(scope) {
        const rows = this.scorecard.rows
        const curRow = rows[scope.$index]

        if (!curRow.fact) {
          rows.splice(scope.$index, 1)
          return
        }

        let rowspan = 1
        for (let i = scope.$index + 1; i < rows.length; i++) {
          const iRow = rows[i]
          if (iRow.fact && curRow.fact.id == iRow.fact.id && curRow.fact.fieldId == iRow.fact.fieldId) {
            rowspan++
          } else {
            break
          }
        }
        rows.splice(scope.$index, rowspan)
      },
      rowKey(row) {
        if (!row._id) row._id = this.$utils.randomCode(4)
        return row._id
      },
      scorecardTemplate() {
        this.$emit('addTemplate')
      },
      saveCallback(callback) {
        let rows = this.scorecard.rows
        if (this.scorecard.weight) {
          rows.forEach((e, i) => {
            if (i > 0) {
              const prevRow = rows[i - 1]
              const curRow = rows[i]
              if (prevRow.fact
                && curRow.fact
                && prevRow.fact.id == curRow.fact.id
                && prevRow.fact.fieldId == curRow.fact.fieldId) {
                rows[i].weight = rows[i - 1].weight
              }
            }
          })
        } else {
          rows.forEach(e => {
            delete e.weight
          })
        }
        const scorecard = this.$utils.copy(this.scorecard)
        this.$utils.clearTmpKey(scorecard)
        callback(JSON.stringify(scorecard))
      },
      getLeftType(fact) {
        return this.$store.getters.getExpressionType({
          type: 'FACT',
          fact
        })
      },
      refresh(assets) {
        if (assets.changeVersion === true) {
          this.scorecard = JSON.parse(assets.content)
        } else {
          this.$emit('refresh',assets)
        }
      },
      changeField(newVal, oldVal, index) {
        if (!oldVal) {
          return
        }

        const rows = this.scorecard.rows
        for (let i = index + 1, len = rows.length; i < len; i++) {
          const row = rows[i]
          if (!row.fact || oldVal.id != row.fact.id || oldVal.fieldId != row.fact.fieldId) {
            break;
          }
          row.fact = {
            id: newVal.id,
            fieldId: newVal.fieldId
          }
        }
      },
      filterRules() {
        const scorecard = JSON.parse(this.assets.content)

        if (this.fireRules.length > 0 && this.showFireRules) {
          const rows = []
          this.fireRules.forEach(rule => {
            const ruleName = this.assets.versionNo
              ? rule.substring(`评分卡-${this.assets.name}_V${this.assets.versionNo}-`.length)
              : rule.substring(`评分卡-${this.assets.name}-`.length)
            const rowNum = parseInt(ruleName.substring(1, ruleName.length - 1))
            if (rowNum) {
              scorecard.rows.forEach((r, i) => {
                if (rowNum == i + 1) {
                  rows.push(r)
                }
              })
            }
          })
          scorecard.rows = rows
        }

        this.scorecard = scorecard
      }
    }
  }
</script>

<style scoped>
</style>
