<template>
  <div style="display: inline-block;">
    <dropdown @command="changeType" v-if="!readonly">
      <span class="el-dropdown-link" v-if="value.type"><i class="el-icon-caret-bottom"></i></span>
      <span class="el-dropdown-link" v-else>请选择</span>
      <dropdown-menu slot="dropdown">
        <dropdown-item v-for="(e, i) in Object.keys(types)" :key="i" :command="e">{{types[e]}}</dropdown-item>
      </dropdown-menu>
    </dropdown>

    <template v-if="value.type == 'ASSIGN'">
      <fact v-model="value.left" scope="ASSIGN" :immediate="childImmediate" :readonly="readonly"></fact>
      <span v-if="rightInputType">=</span>
      <expression
        v-model="value.right"
        v-if="rightInputType"
        :input-type="rightInputType"
        :loop-target="loopTarget"
        :readonly="readonly">
      </expression>
    </template>
    <func
      v-else-if="value.type == 'FUNC'"
      v-model="value.func"
      :immediate="childImmediate"
      :readonly="readonly">
    </func>
    <thirdapi
      v-else-if="value.type == 'THIRDAPI'"
      v-model="value.thirdApi"
      :immediate="childImmediate"
      :readonly="readonly">
    </thirdapi>
    <model
      v-else-if="value.type == 'MODEL'"
      v-model="value.model"
      :immediate="childImmediate"
      :readonly="readonly">
    </model>
  </div>
</template>

<script>
  export default {
    name: "action",
    props: {
      value: {
        type: Object,
        default: function() {
          return {type: 'ASSIGN'}
        }
      },
      readonly: {
        type: Boolean,
        default: false
      },
      loopTarget: Object
    },
    data() {
      return {
        types: {
          ASSIGN: '赋值',
          FUNC: '执行函数',
          // THIRDAPI: '调用接口',
          MODEL: '调用模型'
        },
        childImmediate: false
      }
    },
    computed: {
      rightInputType: function() {
        return this.$store.getters.getExpressionType({
          fact: this.value.left,
          type: 'FACT'
        })
      }
    },
    mounted() {
      if (!this.value.type) {
        this.$set(this.value, 'type', 'ASSIGN')
      }
    },
    methods: {
      changeType(command) {
        if (command != this.value.type) {
          this.$delete(this.value, 'left')
          this.$delete(this.value, 'right')
          this.$delete(this.value, 'func')
          this.$set(this.value, 'type', command)
          this.$emit('input', this.value)
          this.childImmediate = true
        }
      }
    }
  }
</script>

<style scoped>

</style>
