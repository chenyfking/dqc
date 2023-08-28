<template>
  <div style="display: inline-block;">
    <expression v-model="value.left" v-if="!leftType" @draw="$emit('draw')" :loop-target="loopTarget" :readonly="readonly"></expression>
    <op v-model="value.op" v-if="opInputType" :input-type="opInputType" @draw="$emit('draw')" :readonly="readonly"></op>
    <expression v-model="value.right" v-if="rightInputType" :input-type="rightInputType" @draw="$emit('draw')" :loop-target="loopTarget" :readonly="readonly"></expression>
  </div>
</template>

<script>
  export default {
    name: "constraint",
    props: {
      value: {
        type: Object,
        default: function() {
          return {}
        }
      },
      leftType: [String, Object], // 直接指定左侧表达式的类型，不用手工选择，用在决策表和评分卡配置条件列处，左侧由列头指定
      loopTarget: Object,
      readonly: {
        type: Boolean,
        default: false
      }
    },
    computed: {
      opInputType: function() {
        return this.leftType || this.$store.getters.getExpressionType(this.value.left, this.loopTarget)
      },
      rightInputType: function() {
        const op = this.value.op
        if (op === 'STR_LENGTH' || op === 'STR_NOT_LENGTH') {
          return 'Integer'
        }
        const leftType = this.leftType || this.$store.getters.getExpressionType(this.value.left, this.loopTarget)
        if (this.$utils.isCollection(leftType)) {
          const subType = this.$utils.getCollectionSubType(leftType)
          if (this.$utils.isNotBlank(subType)) {
            return subType
          }
        }
        return leftType
      }
    }
  }
</script>

<style scoped>

</style>
