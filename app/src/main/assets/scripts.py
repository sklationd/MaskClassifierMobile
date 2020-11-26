import torch
quantized_torchmodel = torch.jit.load("quantized_best.pt", map_location='cpu')
torch.jit.save(quantized_torchmodel, "best_cpu_quantized.pt")

scripted_torchmodel = torch.jit.load("scripted_best.pt", map_location='cpu')
torch.jit.save(scripted_torchmodel, "best_cpu_scripted.pt")

vulkan_torchmodel = torch.jit.load("vulkan.pt", map_location='cpu')
torch.jit.save(vulkan_torchmodel, "cpu_vulkan.pt")
