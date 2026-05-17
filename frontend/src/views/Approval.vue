<template>
  <div class="approval">
    <el-card shadow="hover">
      <template #header>
        <span>待我审批</span>
      </template>
      <el-table :data="approvalList" border stripe>
        <el-table-column prop="id" label="报销单号" width="250" />
        <el-table-column prop="employeeName" label="申请人" width="100" />
        <el-table-column prop="department" label="部门" width="100" />
        <el-table-column prop="expenseType" label="费用类型" width="120">
          <template #default="scope">
            {{ getExpenseTypeName(scope.row.expenseType) }}
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="100">
          <template #default="scope">
            ¥{{ scope.row.amount }}
          </template>
        </el-table-column>
        <el-table-column prop="description" label="费用说明" />
        <el-table-column prop="invoiceNumber" label="发票编号" width="150" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusName(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" type="success" @click="approve(scope.row)">批准</el-button>
            <el-button size="small" type="danger" @click="reject(scope.row)">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="rejectVisible" title="驳回报销" width="500px">
      <el-form :model="rejectForm" label-width="80px">
        <el-form-item label="驳回原因" prop="reason">
          <el-input v-model="rejectForm.reason" type="textarea" :rows="4" placeholder="请输入驳回原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'Approval',
  data() {
    return {
      approvalList: [],
      rejectVisible: false,
      currentApproval: null,
      rejectForm: {
        reason: ''
      },
      currentUserId: 'E002'
    }
  },
  mounted() {
    this.loadCurrentUser()
    this.loadPendingApprovals()
  },
  activated() {
    this.loadCurrentUser()
    this.loadPendingApprovals()
  },
  watch: {
    $route(to) {
      if (to.path === '/approval') {
        this.loadCurrentUser()
        this.loadPendingApprovals()
      }
    }
  },
  methods: {
    loadCurrentUser() {
      const app = this.$root
      if (app.$children && app.$children[0]) {
        const mainApp = app.$children[0]
        if (mainApp.currentUserId) {
          this.currentUserId = mainApp.currentUserId
        }
      }
    },
    async loadPendingApprovals() {
      try {
        const response = await axios.get(`http://localhost:8003/api/reimbursement/pending/${this.currentUserId}`)
        this.approvalList = response.data
      } catch (error) {
        console.error('加载审批列表失败', error)
      }
    },
    getExpenseTypeName(type) {
      const names = {
        TRANSPORTATION: '交通费',
        ACCOMMODATION: '住宿费',
        MEAL: '餐饮费',
        OFFICE_SUPPLY: '办公用品',
        TRAVEL: '差旅费',
        OTHER: '其他'
      }
      return names[type] || type
    },
    getStatusName(status) {
      const names = {
        DRAFT: '草稿',
        PENDING_APPROVAL: '待部门审批',
        MANAGER_APPROVED: '经理已批准',
        FINANCE_REVIEW: '待财务复核',
        APPROVED: '已批准',
        REJECTED: '已驳回',
        PAYMENT_PENDING: '待打款',
        COMPLETED: '已完成'
      }
      return names[status] || status
    },
    getStatusType(status) {
      const types = {
        DRAFT: 'info',
        PENDING_APPROVAL: 'warning',
        MANAGER_APPROVED: 'primary',
        FINANCE_REVIEW: 'primary',
        APPROVED: 'success',
        REJECTED: 'danger',
        PAYMENT_PENDING: 'warning',
        COMPLETED: 'success'
      }
      return types[status] || 'info'
    },
    async approve(row) {
      try {
        let endpoint = ''
        
        if (row.status === 'PENDING_APPROVAL') {
          endpoint = `approve/manager/${row.id}?approverId=${this.currentUserId}&comment=同意`
        } else if (row.status === 'FINANCE_REVIEW' || row.status === 'MANAGER_APPROVED') {
          endpoint = `approve/finance/${row.id}?approverId=${this.currentUserId}&comment=财务复核通过`
        } else {
          endpoint = `approve/manager/${row.id}?approverId=${this.currentUserId}&comment=同意`
        }

        await axios.post(`http://localhost:8003/api/reimbursement/${endpoint}`)
        this.$message.success('审批通过')
        this.loadPendingApprovals()
      } catch (error) {
        this.$message.error(error.response?.data?.message || '审批失败')
      }
    },
    getCurrentUserRole() {
      const app = this.$root
      if (app.$children && app.$children[0]) {
        const mainApp = app.$children[0]
        if (mainApp.currentUser) {
          return mainApp.currentUser.role
        }
      }
      return 'MANAGER'
    },
    reject(row) {
      this.currentApproval = row
      this.rejectForm.reason = ''
      this.rejectVisible = true
    },
    async confirmReject() {
      if (!this.rejectForm.reason) {
        this.$message.warning('请输入驳回原因')
        return
      }
      try {
        await axios.post(
          `http://localhost:8003/api/reimbursement/reject/${this.currentApproval.id}?approverId=${this.currentUserId}&reason=${encodeURIComponent(this.rejectForm.reason)}`
        )
        this.$message.success('驳回成功')
        this.rejectVisible = false
        this.loadPendingApprovals()
      } catch (error) {
        this.$message.error('驳回失败')
      }
    }
  }
}
</script>

<style scoped>
.approval {
  padding: 0;
}
</style>
