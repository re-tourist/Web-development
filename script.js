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
    const container = document.querySelector('.container');
    const projectCards = document.querySelectorAll('.project-card');
    const toggleBtn = document.getElementById('toggleButton');

    setTimeout(() => { container.classList.add('loaded'); }, 300);
    projectCards.forEach((card, index) => {
        setTimeout(() => { card.classList.add('loaded'); }, 500 + index * 100);
    });

    // 悬停仅垂直上浮
    projectCards.forEach(card => {
        card.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-6px)';
        });
        card.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0)';
        });

        // 点击/回车打开对应项目
        card.addEventListener('click', () => {
            const name = card.getAttribute('data-project');
            if (name) openProject(name);
        });
        card.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                const name = card.getAttribute('data-project');
                if (name) openProject(name);
            }
        });
    });

    // 折叠/展开预览：只显示标题
    if (toggleBtn) {
        toggleBtn.addEventListener('click', () => {
            container.classList.toggle('collapsed');
            const collapsed = container.classList.contains('collapsed');
            toggleBtn.setAttribute('aria-pressed', collapsed ? 'true' : 'false');
            toggleBtn.title = collapsed ? '展开预览' : '折叠预览';
        });
        toggleBtn.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                toggleBtn.click();
            }
        });
    }

    // 初始化樱花飘落特效（保留）
    if (window.initSakura) {
        window.initSakura({
            count: 36,
            zIndex: 1,
            speedY: 0.8,
            speedYVar: 1.0,
            speedXVar: 0.8,
            rotSpeedVar: 0.03
        });
    }
});