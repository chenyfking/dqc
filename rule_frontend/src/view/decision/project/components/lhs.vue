<template>
  <div :style="{position: 'relative', height: height + 'px', 'white-space': 'nowrap'}" ref="container">
    <div v-for="(constraint, index) in constraints" :key="constraint._id">
      <div v-if="constraint.conjunction" :key="constraint._id" class="conjunction" :ref="constraint._id" :style="{position: 'absolute', left: constraint._location[0] * 80 + 'px', top: constraint._location[1] * 30 + 'px'}">
        <dropdown @command="command => selectCondition(command, constraint)" v-if="!readonly">
          <span style="cursor: pointer;">{{$conjunction[constraint.conjunction]}}<i class="el-icon-arrow-down el-icon--right"></i></span>
          <dropdown-menu slot="dropdown">
            <dropdown-item command="AND">并且</dropdown-item>
            <dropdown-item command="OR">或者</dropdown-item>
            <dropdown-item command="add-condition">添加条件</dropdown-item>
            <dropdown-item command="add-join-condition" v-if="!leftType">添加联合条件</dropdown-item>
            <dropdown-item command="del" divided v-if="!leftType">删除</dropdown-item>
          </dropdown-menu>
        </dropdown>
        <span v-else>{{$conjunction[constraint.conjunction]}}</span>
      </div>
      <div
        v-else
        :key="constraint._id"
        :ref="constraint._id"
        :style="{position: 'absolute', left: constraint._location[0] * 80 + 'px', top: constraint._location[1] * 30 + 'px', paddingRight: '10px'}">
        <constraint v-model="constraints[index]" :left-type="leftType" @draw="draw" :loop-target="loopTarget" :readonly="readonly"></constraint>
        <template v-if="!readonly">
          <i class="el-icon-delete icon-btn-mini" @click="selectCondition('del', constraint)"></i>
          <i class="el-icon-document-copy icon-btn-mini" @click="copyConstraint(constraint)"></i>
        </template>
      </div>
    </div>
    <svg v-if="lines.length > 0" :width="getSvgWidth()" height="100%"
         xmlns="http://www.w3.org/2000/svg" version="1.1" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:svgjs="http://svgjs.com/svgjs">
      <path v-for="d in lines" :d="d" fill="none" stroke="#777777" stroke-width="1"></path>
    </svg>
  </div>
</template>

<script>
  import constraint from './constraint'

  export default {
    name: "lhs",
    components: {
      'constraint': constraint
    },
    props: {
      value: {
        type: Object,
        default: function() {
          const defaultValue = {
            constraint: {
              conjunction: 'AND',
              constraints: [],
              _location: [0, 0]
            }
          }
          this.$emit('input', defaultValue)
          return defaultValue
        }
      },
      leftType: [String, Object], // 直接指定左侧表达式的类型，不用手工选择，用在决策表和评分卡配置条件列处，左侧由列头指定
      loopTarget: Object,
      readonly: {
        type: Boolean,
        default: false
      }
    },
    data() {
      if (!this.value.constraint) {
        this.$set(this.value, 'constraint', {
          conjunction: 'AND',
          constraints: [],
          _location: [0, 0]
        })
        this.$emit('input', this.value)
      }

      return {
        height: 30,
        constraints: [this.value.constraint],
        lines: []
      }
    },
    created() {
      this.traverse()
    },
    mounted() {
      this.draw()
    },
    methods: {
      selectCondition(command, constraint) {
        if (command == 'AND' || command == 'OR') {
          constraint.conjunction = command
        } else if (command == 'add-condition') {
          constraint.constraints.push({})
          this.traverse()
          this.draw()
        } else if (command == 'add-join-condition') {
          constraint.constraints.push({
            conjunction: 'AND',
            constraints: []
          })
          this.traverse()
          this.draw()
        } else if (command == 'del') {
          this.delNode(constraint._id)
          this.traverse()
          this.draw()
        }
      },
      traverse() {
        const root = this.value.constraint
        this.getNodeDeep(root)

        let nodes = []
        let x = 0, d = 0
        let stack = [root], nexts = []

        while (stack.length > 0) {
          d = 0
          stack.forEach(node => {
            if (!node._id) node._id = this.$utils.randomCode(6)
            const parentY = node._parentY ? node._parentY : 0
            if (d < parentY) {
              d = parentY
            }
            node._location = [x, d]
            d += node._deep
            nodes.push(node)

            if (node.constraints && node.constraints.length > 0) {
              node.constraints.forEach(child => {
                child._parentY = node._location[1]
                child._parentId = node._id
                nexts.push(child)
              })
            }
          })
          x++
          stack = nexts
          nexts = []
        }
        this.height = 30 * root._deep
        this.constraints = nodes
      },
      draw() {
        this.$nextTick(function () {
          setTimeout(() => {
            let stack = [this.value.constraint], nexts = [], lines = []
            while (stack.length > 0) {
              stack.forEach(node => {
                if (node.constraints && node.constraints.length > 0) {
                  node.constraints.forEach(child => {
                    const el1 = this.$refs[node._id][0], el2 = this.$refs[child._id][0]
                    if (el1 && el2) {
                      const x1 = parseInt(el1.style.left) + el1.offsetWidth - 5
                      const y1 = parseInt(el1.style.top) + el1.offsetHeight / 2
                      const x2 = parseInt(el2.style.left)
                      const y2 = parseInt(el2.style.top) + el2.offsetHeight / 2
                      const d = 'M' + x1 + ' ' + y1 + 'C' + x1 + ' ' + y2 + ' ' + x1 + ' ' + y2 + ' ' + x2 + ' ' + y2
                      lines.push(d)
                      nexts.push(child)
                    }
                  })
                }
              })
              stack = nexts
              nexts = []
            }
            this.lines = lines
          }, 0)
        })
      },
      traverseNode(node, x, y) {
        node.location = [x, y]
        if (node.constraints.length > 0) {
          x++
          y = node.location[1]
          node.constraints.forEach(e => {
            this.traverseNode(e, x, y++)
          })
        }
      },
      getNodeDeep(node) {
        if (!node.constraints || node.constraints.length == 0) {
          node._deep = 1;
          return 1
        }

        let deep = 0
        node.constraints.forEach(e => {
          deep += this.getNodeDeep(e)
        })
        node._deep = deep
        return deep
      },
      delNode(_id) {
        if (this.value.constraint._id == _id) {
          this.value.constraint.constraints = []
        } else {
          let stack = [this.value.constraint];
          while (stack.length > 0) {
            const node = stack.pop();
            if (node.constraints && node.constraints.length > 0) {
              node.constraints.forEach((e, i) => {
                if (e._id == _id) {
                  node.constraints.splice(i, 1)
                  return false
                }
                stack.push(e)
              })
            }
          }
        }
      },
      getSvgWidth() {
        return this.$refs.container.scrollWidth
      },
      copyConstraint(constraint) {
        let parent, index
        this.constraints.some(e => {
          if (e._id == constraint._parentId) {
            parent = e
            parent.constraints.some((e2, i) => {
              if (e2._id == constraint._id) {
                index = i
                return true
              }
            })
            return true
          }
        })
        if (!parent) return
        let copy = this.$utils.copy(constraint)
        this.$utils.clearTmpKey(copy)
        parent.constraints.splice(index + 1, 0, copy)
        this.traverse()
        this.draw()
      },

    }
  }
</script>

<style scoped>
  .conjunction {
    border: solid #999 1px;
    padding: 1px;
    background: #fff;
    border-radius: 4px;
  }
</style>
