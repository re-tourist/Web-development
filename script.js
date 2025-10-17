// 项目面板切换功能
function toggleProjectPanel() {
    const panel = document.getElementById('projectPanel');
    const button = document.getElementById('toggleButton');
    
    panel.classList.toggle('hidden');
    
    if (panel.classList.contains('hidden')) {
        button.innerHTML = '☰';
        button.style.right = '20px';
    } else {
        button.innerHTML = '✕';
        button.style.right = '440px';
    }
}

// 项目导航函数
function openProject(projectName) {
    window.location.href = `./${projectName}/index.html`;
}

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', function() {
    // 页面加载动画
    const container = document.querySelector('.container');
    const projectCards = document.querySelectorAll('.project-card');
    
    setTimeout(() => {
        container.classList.add('loaded');
    }, 300);
    
    projectCards.forEach((card, index) => {
        setTimeout(() => {
            card.classList.add('loaded');
        }, 500 + index * 100);
    });
    
    // 项目卡片悬停效果增强
    projectCards.forEach(card => {
        card.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-3px) scale(1.02)';
        });
        
        card.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0) scale(1)';
        });
    });
});