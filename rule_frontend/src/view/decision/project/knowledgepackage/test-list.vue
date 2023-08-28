<template>
  <div @click="openListDialog">
    <el-link icon="el-icon-edit" v-if="list.length == 0"></el-link>
    <div v-else>
      <dropdown placement="right" trigger="hover">
        <el-link icon="el-icon-edit"></el-link>
        <dropdown-menu slot="dropdown">
          <dropdown-item  v-for="(e,i) in list" :key="i" >值{{e.value}}</dropdown-item>
        </dropdown-menu>
      </dropdown>
    </div>

    <el-dialog title="设置List对象数据" :visible.sync="show" append-to-body :close-on-click-modal="false">
      <div class="toolbar">
        <el-button type="primary" icon="el-icon-plus" @click="addLine">添加行</el-button>
      </div>

      <el-table ref="table" :data="list" class="tb-edit"
                highlight-current-row border @cell-click="cellClick">
        <el-table-column label="值" align="center">
          <template slot-scope="scope">
            <el-input-number v-if="$utils.isNumber(type)"
                             v-model.trim="scope.row.value"
                             :ref="'input_value_' + scope.row.id"
                             :controls="false"></el-input-number>
            <el-select v-else-if="$utils.isBoolean(type)"
                       :ref="'input_value_' + scope.row.id"
                       v-model="scope.row.value">
              <el-option value="true" label="是"></el-option>
              <el-option value="false" label="否"></el-option>
            </el-select>
            <el-date-picker v-else-if="$utils.isDate(type)" placeholder="请选择"
                            v-model="scope.row.value" type="datetime"
                            :ref="'input_value_' + scope.row.id"
                            value-format="yyyy-MM-dd HH:mm:ss" align="center">
            </el-date-picker>
            <el-input v-model.trim="scope.row.value" :ref="'input_value_' + scope.row.id" v-else></el-input>

            <span v-if="$utils.isBoolean(type)">
              {{!scope.row.value ? '' : (scope.row.value == 'true' ? '是' : '否')}}
            </span>
            <span v-else>{{scope.row.value}}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center">
          <template slot-scope="scope">
            <el-button type="text" @click="delLine(scope.$index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div slot="footer">
        <el-button @click="show = false" icon="el-icon-error">取 消</el-button>
        <el-button type="primary" icon="el-icon-success" @click="submit">
          确 定
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
  export default {
    name: "test-list",
    props: {
      value: Array,
      type: String
    },
    data() {
      return {
        show: false,
        list: []
      }
    },
    methods: {
      openListDialog() {
        this.show = true
        this.list = []
        if (this.value) {
          this.value.forEach(e => {
            this.list.push({
              id: this.$utils.randomCode(6),
              value: e
            })
          })
        }
      },
      addLine() {
        let row = {
          id: this.$utils.randomCode(6)
        }
        if (this.$utils.isNumber(this.type)) {
          row.value = 0
        } else {
          row.value = ''
        }
        this.list.push(row)
        setTimeout(() => {
          this.$refs['table'].setCurrentRow(row)
        }, 10)
        setTimeout(() => {
          this.$refs['input_value_' + row.id].focus()
        }, 10)
      },
      delLine(index) {
        this.list.splice(index, 1)
      },
      cellClick(row, column, cell, event) {
        if (column.label = '值') {
          setTimeout(() => {
            this.$refs['input_value_' + row.id] && this.$refs['input_value_' + row.id].focus()
          }, 0)
        }
      },
      submit() {
        if (this.list.length == 0) {
          this.$message.warning('请添加行')
          return
        }

        let valid = true
        this.list.some(e => {
          if (this.$utils.isBlank(e.value)) {
            valid = false
            return true
          }
        })
        if (!valid) {
          this.$message.warning('请补充完整数据')
          return
        }

        let rows = []
        this.list.forEach(e => {
          rows.push(e.value)
        })

        this.$emit('input', rows)
        this.show = false
      }
    }
  }
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
